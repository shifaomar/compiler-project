package absyn;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * TM code generation. Person 2: control flow, functions, calls, compound scopes, full OpExp.
 * Person 1 (Athina): prelude, I/O, initial arithmetic tests — merged with Person 2.
 */
public class CodeGenerator implements AbsynVisitor {
    private final PrintWriter out;

    private final Map<String, Integer> globalVars = new HashMap<>();
    private final Map<String, Integer> funcEntries = new HashMap<>();

    private static final int AC = 0;
    private static final int AC1 = 1;
    private static final int PC = 7;
    private static final int FP = 5;
    private static final int GP = 6;

    private static final int OFP_FO = 0;
    private static final int RET_FO = -1;
    private static final int INIT_FO = -2;

    private int emitLoc = 0;
    private int highEmitLoc = 0;
    private int mainEntry = -1;
    private int globalOffset = 0;

    private int inputEntryLoc = -1;
    private int outputEntryLoc = -1;

    /** non-main function bodies: skip from end of I/O to finale */
    private int jumpOverUserFunctionsLoc = -1;

    /** per-function: stack of block scopes (name -> offset from FP) */
    private final Deque<Map<String, Integer>> scopeStack = new ArrayDeque<>();

    /** next slot below current locals (always negative) */
    private int localOffset = -2;

    public CodeGenerator(PrintWriter out) {
        this.out = out;
    }

    private void emitComment(String comment) {
        out.println("* " + comment);
    }

    private void emitRO(String op, int r, int s, int t, String comment) {
        out.printf("%3d: %5s %d, %d, %d", emitLoc, op, r, s, t);
        if (comment != null && !comment.isEmpty()) {
            out.printf("\t%s", comment);
        }
        out.println();
        emitLoc++;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    private void emitRM(String op, int r, int d, int s, String comment) {
        out.printf("%3d: %5s %d, %d(%d)", emitLoc, op, r, d, s);
        if (comment != null && !comment.isEmpty()) {
            out.printf("\t%s", comment);
        }
        out.println();
        emitLoc++;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    private void emitRMAbs(String op, int r, int a, String comment) {
        out.printf("%3d: %5s %d, %d(%d)", emitLoc, op, r, a - (emitLoc + 1), PC);
        if (comment != null && !comment.isEmpty()) {
            out.printf("\t%s", comment);
        }
        out.println();
        emitLoc++;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    private int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
        return i;
    }

    private void emitBackup(int loc) {
        emitLoc = loc;
    }

    private void emitRestore() {
        emitLoc = highEmitLoc;
    }

    /** if AC == 0, PC = absolute address dest */
    private void emitJumpToAbsIfZero(int destAbs, String comment) {
        emitRM("JEQ", AC, destAbs - (emitLoc + 1), PC, comment);
    }

    /** unconditional jump to absolute address */
    private void emitJumpToAbs(int destAbs, String comment) {
        emitRMAbs("LDA", PC, destAbs, comment);
    }

    /** load left and right; AC = left - right (for compare / ordering) */
    private void emitLoadBinaryOperands(OpExp n, int level, int tmpSlot) {
        if (n.left instanceof OpExp) {
            genOpExp((OpExp) n.left, level, tmpSlot - 1);
        } else {
            n.left.accept(this, level);
        }
        emitRM("ST", AC, tmpSlot, FP, "save left operand");
        if (n.right instanceof OpExp) {
            genOpExp((OpExp) n.right, level, tmpSlot - 1);
        } else {
            n.right.accept(this, level);
        }
        emitRM("LD", AC1, tmpSlot, FP, "load left operand");
        emitRO("SUB", AC, AC1, AC, "left - right");
    }

    /** AC has (left - right); leave 0 or 1 in AC */
    private void emitBoolFromDiff(int op) {
        int skip = emitSkip(1);
        emitRM("LDC", AC, 0, 0, "cmp false");
        int skipMerge = emitSkip(1);
        int trueLabel = emitLoc;
        emitRM("LDC", AC, 1, 0, "cmp true");
        int merge = emitLoc;
        emitBackup(skipMerge);
        emitJumpToAbs(merge, "cmp merge");
        emitRestore();
        emitBackup(skip);
        switch (op) {
            case OpExp.EQ:
                emitRM("JEQ", AC, trueLabel - (emitLoc + 1), PC, "eq");
                break;
            case OpExp.NE:
                emitRM("JNE", AC, trueLabel - (emitLoc + 1), PC, "ne");
                break;
            case OpExp.LT:
                emitRM("JLT", AC, trueLabel - (emitLoc + 1), PC, "lt");
                break;
            case OpExp.LE:
                emitRM("JLE", AC, trueLabel - (emitLoc + 1), PC, "le");
                break;
            case OpExp.GT:
                emitRM("JGT", AC, trueLabel - (emitLoc + 1), PC, "gt");
                break;
            case OpExp.GE:
                emitRM("JGE", AC, trueLabel - (emitLoc + 1), PC, "ge");
                break;
            default:
                throw new RuntimeException("Code generation error: bad compare op");
        }
        emitRestore();
    }

    private void genOpExp(OpExp n, int level, int tmpSlot) {
        if (n.op == OpExp.ASSIGN) {
            throw new RuntimeException("internal: assignment should be AssignExp, not OpExp.ASSIGN");
        }

        if (n.op >= OpExp.EQ && n.op <= OpExp.GE) {
            emitLoadBinaryOperands(n, level, tmpSlot);
            emitBoolFromDiff(n.op);
            return;
        }

        if (n.op >= OpExp.PLUS && n.op <= OpExp.SLASH) {
            if (n.left instanceof OpExp) {
                genOpExp((OpExp) n.left, level, tmpSlot - 1);
            } else {
                n.left.accept(this, level);
            }
            emitRM("ST", AC, tmpSlot, FP, "save left operand");
            if (n.right instanceof OpExp) {
                genOpExp((OpExp) n.right, level, tmpSlot - 1);
            } else {
                n.right.accept(this, level);
            }
            emitRM("LD", AC1, tmpSlot, FP, "load left operand");
            switch (n.op) {
                case OpExp.PLUS:
                    emitRO("ADD", AC, AC1, AC, "op +");
                    break;
                case OpExp.MINUS:
                    emitRO("SUB", AC, AC1, AC, "op -");
                    break;
                case OpExp.STAR:
                    emitRO("MUL", AC, AC1, AC, "op *");
                    break;
                case OpExp.SLASH:
                    emitRO("DIV", AC, AC1, AC, "op /");
                    break;
                default:
                    throw new RuntimeException("Code generation error: unsupported arithmetic op");
            }
            return;
        }

        throw new RuntimeException("Code generation error: unsupported OpExp operator");
    }

    private Integer lookupVar(String name) {
        for (Map<String, Integer> m : scopeStack) {
            if (m.containsKey(name)) {
                return m.get(name);
            }
        }
        return globalVars.get(name);
    }

    /** Globals use GP; locals/params use FP — even when reading code inside a function body. */
    private boolean isGlobalBinding(String name) {
        for (Map<String, Integer> m : scopeStack) {
            if (m.containsKey(name)) {
                return false;
            }
        }
        return globalVars.containsKey(name);
    }

    private void emitLoadVar(String name) {
        Integer off = lookupVar(name);
        if (off == null) {
            throw new RuntimeException("Code generation error: unknown variable '" + name + "'");
        }
        if (isGlobalBinding(name)) {
            emitRM("LD", AC, off, GP, "load global " + name);
        } else {
            emitRM("LD", AC, off, FP, "load " + name);
        }
    }

    private void emitStoreToVar(String name) {
        Integer off = lookupVar(name);
        if (off == null) {
            throw new RuntimeException("Code generation error: unknown variable '" + name + "'");
        }
        if (isGlobalBinding(name)) {
            emitRM("ST", AC, off, GP, "store global " + name);
        } else {
            emitRM("ST", AC, off, FP, "store " + name);
        }
    }

    @Override
    public void visit(Program n, int level) {
        emitComment("C- compilation to TM code");

        emitComment("standard prelude");
        emitRM("LD", GP, 0, AC, "load gp with maxaddress");
        emitRM("LDA", FP, 0, GP, "copy gp to fp");
        emitRM("ST", AC, 0, AC, "clear location 0");

        int savedLoc = emitSkip(1);

        emitIORoutines();

        int afterIO = emitSkip(0);
        emitBackup(savedLoc);
        emitRMAbs("LDA", PC, afterIO, "jump around i/o routines");
        emitRestore();

        jumpOverUserFunctionsLoc = emitSkip(1);

        if (n.declarations != null) {
            n.declarations.accept(this, level);
        }

        if (mainEntry == -1) {
            throw new RuntimeException("Code generation error: missing main function");
        }

        int finaleStart = emitLoc;
        emitBackup(jumpOverUserFunctionsLoc);
        emitJumpToAbs(finaleStart, "jump over function bodies to finale");
        emitRestore();

        emitComment("finale");
        emitRM("ST", FP, globalOffset + OFP_FO, FP, "push ofp");
        emitRM("LDA", FP, globalOffset, FP, "push frame");
        emitRM("LDA", AC, 1, PC, "load ac with return pointer");
        emitRMAbs("LDA", PC, mainEntry, "jump to main");
        emitRM("LD", FP, OFP_FO, FP, "pop frame");
        emitRO("HALT", 0, 0, 0, "done");
    }

    private void emitIORoutines() {
        inputEntryLoc = emitLoc;
        emitComment("code for input routine");
        emitRM("ST", AC, RET_FO, FP, "store return");
        emitRO("IN", AC, 0, 0, "input");
        emitRM("LD", PC, RET_FO, FP, "return to caller");

        outputEntryLoc = emitLoc;
        emitComment("code for output routine");
        emitRM("ST", AC, RET_FO, FP, "store return");
        emitRM("LD", AC, INIT_FO, FP, "load output value");
        emitRO("OUT", AC, 0, 0, "output");
        emitRM("LD", PC, RET_FO, FP, "return to caller");
    }

    @Override
    public void visit(DeclList n, int level) {
        while (n != null) {
            if (n.head != null) {
                n.head.accept(this, level);
            }
            n = n.tail;
        }
    }

    @Override
    public void visit(VarDecl n, int level) {
        if (!scopeStack.isEmpty()) {
            throw new RuntimeException("internal: VarDecl in non-global should use compound/function handling");
        }
        globalOffset--;
        globalVars.put(n.name, globalOffset);
    }

    @Override
    public void visit(ArrayDecl n, int level) {
        globalOffset -= (n.size + 1);
        globalVars.put(n.name, globalOffset + 1);
    }

    @Override
    public void visit(FunDecl n, int level) {
        funcEntries.put(n.name, emitLoc);
        if ("main".equals(n.name)) {
            mainEntry = emitLoc;
        }

        emitComment("function " + n.name);
        emitRM("ST", AC, RET_FO, FP, "store return");

        scopeStack.clear();
        Map<String, Integer> frame = new HashMap<>();
        scopeStack.push(frame);

        int pOff = -2;
        ParamList pl = n.params;
        while (pl != null && pl.head != null) {
            frame.put(pl.head.name, pOff);
            pOff--;
            pl = pl.tail;
        }
        localOffset = pOff;

        if (n.declarations != null) {
            DeclList dl = n.declarations;
            while (dl != null && dl.head != null) {
                if (dl.head instanceof VarDecl) {
                    VarDecl vd = (VarDecl) dl.head;
                    localOffset--;
                    frame.put(vd.name, localOffset);
                } else if (dl.head instanceof ArrayDecl) {
                    throw new RuntimeException("Code generation error: array locals not implemented (Person 3)");
                }
                dl = dl.tail;
            }
        }

        if (n.body != null) {
            n.body.accept(this, level);
        }

        emitRM("LD", PC, RET_FO, FP, "return to caller");
        scopeStack.clear();
    }

    @Override
    public void visit(FunPrototype n, int level) {
    }

    @Override
    public void visit(ParamList n, int level) {
    }

    @Override
    public void visit(Param n, int level) {
    }

    @Override
    public void visit(Type n, int level) {
    }

    @Override
    public void visit(StmtList n, int level) {
        while (n != null) {
            if (n.head != null) {
                n.head.accept(this, level);
            }
            n = n.tail;
        }
    }

    @Override
    public void visit(CompoundStmt n, int level) {
        if (scopeStack.isEmpty()) {
            if (n.declarations != null) {
                n.declarations.accept(this, level);
            }
            if (n.statements != null) {
                n.statements.accept(this, level);
            }
            return;
        }

        scopeStack.push(new HashMap<>());
        if (n.declarations != null) {
            DeclList dl = n.declarations;
            while (dl != null && dl.head != null) {
                if (dl.head instanceof VarDecl) {
                    VarDecl vd = (VarDecl) dl.head;
                    localOffset--;
                    scopeStack.peek().put(vd.name, localOffset);
                } else if (dl.head instanceof ArrayDecl) {
                    throw new RuntimeException("Code generation error: array in compound not implemented (Person 3)");
                }
                dl = dl.tail;
            }
        }
        if (n.statements != null) {
            n.statements.accept(this, level);
        }
        int innerLocals = scopeStack.peek().size();
        scopeStack.pop();
        localOffset += innerLocals;
    }

    @Override
    public void visit(IfStmt n, int level) {
        n.test.accept(this, level);
        int skipThenHole = emitSkip(1);

        if (n.thenpart != null) {
            n.thenpart.accept(this, level);
        }

        if (n.elsepart != null) {
            int skipElseHole = emitSkip(1);
            int elseStart = emitLoc;

            emitBackup(skipThenHole);
            emitJumpToAbsIfZero(elseStart, "if false jump to else");
            emitRestore();

            n.elsepart.accept(this, level);

            int merge = emitLoc;
            emitBackup(skipElseHole);
            emitJumpToAbs(merge, "after else");
            emitRestore();
        } else {
            int merge = emitLoc;
            emitBackup(skipThenHole);
            emitJumpToAbsIfZero(merge, "if false skip then");
            emitRestore();
        }
    }

    @Override
    public void visit(WhileStmt n, int level) {
        int loopStart = emitLoc;
        n.test.accept(this, level);
        int exitHole = emitSkip(1);

        if (n.body != null) {
            n.body.accept(this, level);
        }
        emitJumpToAbs(loopStart, "while: loop");

        int exitLabel = emitLoc;
        emitBackup(exitHole);
        emitJumpToAbsIfZero(exitLabel, "while: exit if false");
        emitRestore();
    }

    @Override
    public void visit(ReturnStmt n, int level) {
        if (n.expr != null) {
            n.expr.accept(this, level);
        }
        emitRM("LD", PC, RET_FO, FP, "return");
    }

    @Override
    public void visit(ExpStmt n, int level) {
        if (n.expr != null) {
            n.expr.accept(this, level);
        }
    }

    @Override
    public void visit(NullStmt n, int level) {
    }

    @Override
    public void visit(ExpList n, int level) {
        while (n != null) {
            if (n.head != null) {
                n.head.accept(this, level);
            }
            n = n.tail;
        }
    }

    @Override
    public void visit(OpExp n, int level) {
        genOpExp(n, level, localOffset - 1);
    }

    @Override
    public void visit(AssignExp n, int level) {
        if (n.lhs instanceof IdExp) {
            IdExp lhs = (IdExp) n.lhs;
            n.rhs.accept(this, level);
            emitStoreToVar(lhs.name);
            return;
        }
        if (n.lhs instanceof IndexExp) {
            IndexExp lhs = (IndexExp) n.lhs;
            if (!(lhs.array instanceof IdExp)) {
                throw new RuntimeException("Code generation error: array expression not supported");
            }
            String arrName = ((IdExp) lhs.array).name;
            Integer base = globalVars.get(arrName);
            if (base == null) {
                throw new RuntimeException("Code generation error: unknown array '" + arrName + "'");
            }
            n.rhs.accept(this, level);
            int tRhs = localOffset - 1;
            emitRM("ST", AC, tRhs, FP, "rhs");
            lhs.index.accept(this, level);
            emitRM("LDA", AC1, base, GP, "array base");
            emitRO("ADD", AC, AC1, AC, "elem addr");
            emitRM("LD", AC1, tRhs, FP, "rhs");
            emitRM("ST", AC1, 0, AC, "store to array");
            return;
        }
        throw new RuntimeException("Code generation error: unsupported lhs in assignment");
    }

    private void emitCallInput() {
        int frame = 4;
        emitRM("ST", FP, -frame, FP, "push ofp");
        emitRM("LDA", FP, -frame, FP, "push frame");
        emitRM("LDA", AC, 1, PC, "load ac with ret ptr");
        emitRMAbs("LDA", PC, inputEntryLoc, "jump to input");
        emitRM("LD", FP, 0, FP, "pop frame");
    }

    private void emitCallOutput() {
        int frame = 4;
        emitRM("ST", AC, -2, FP, "output arg");
        emitRM("ST", FP, -frame, FP, "push ofp");
        emitRM("LDA", FP, -frame, FP, "push frame");
        emitRM("LDA", AC, 1, PC, "load ac with ret ptr");
        emitRMAbs("LDA", PC, outputEntryLoc, "jump to output");
        emitRM("LD", FP, 0, FP, "pop frame");
    }

    private int countArgs(ExpList args) {
        int c = 0;
        ExpList p = args;
        while (p != null) {
            if (p.head != null) c++;
            p = p.tail;
        }
        return c;
    }

    @Override
    public void visit(CallExp n, int level) {
        if ("input".equals(n.name)) {
            if (n.args != null && countArgs(n.args) != 0) {
                throw new RuntimeException("input() takes no arguments");
            }
            emitCallInput();
            return;
        }
        if ("output".equals(n.name)) {
            if (n.args == null || countArgs(n.args) != 1) {
                throw new RuntimeException("output() expects one argument");
            }
            n.args.head.accept(this, level);
            emitCallOutput();
            return;
        }

        Integer entry = funcEntries.get(n.name);
        if (entry == null) {
            throw new RuntimeException("Code generation error: unknown function '" + n.name + "'");
        }

        int argc = countArgs(n.args);
        ExpList a = n.args;
        int i = 0;
        while (a != null && a.head != null) {
            a.head.accept(this, level);
            emitRM("ST", AC, localOffset - argc - i, FP, "arg " + i);
            i++;
            a = a.tail;
        }

        int ofpSlot = argc > 0 ? (localOffset - argc + 2) : (localOffset - 1);
        emitRM("ST", FP, ofpSlot, FP, "push ofp");
        emitRM("LDA", FP, ofpSlot, FP, "push frame");
        emitRM("LDA", AC, 1, PC, "load ac with ret ptr");
        emitRMAbs("LDA", PC, entry, "call " + n.name);
        emitRM("LD", FP, 0, FP, "pop frame");
    }

    @Override
    public void visit(IndexExp n, int level) {
        if (!(n.array instanceof IdExp)) {
            throw new RuntimeException("Code generation error: indexed array must be a name");
        }
        String arrName = ((IdExp) n.array).name;
        Integer base = globalVars.get(arrName);
        if (base == null) {
            throw new RuntimeException("Code generation error: unknown array '" + arrName + "'");
        }
        n.index.accept(this, level);
        emitRM("LDA", AC1, base, GP, "array base");
        emitRO("ADD", AC, AC1, AC, "elem addr");
        emitRM("LD", AC, 0, AC, "load array elem");
    }

    @Override
    public void visit(IdExp n, int level) {
        emitLoadVar(n.name);
    }

    @Override
    public void visit(NumExp n, int level) {
        emitRM("LDC", AC, n.value, 0, "load const");
    }
}
