package absyn;

import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;

public class CodeGenerator implements AbsynVisitor {
    private final PrintWriter out;

    private final Map<String, Integer> globalVars = new HashMap<>();

    // TM register conventions
    private static final int AC = 0;
    private static final int AC1 = 1;
    private static final int FP = 5;
    private static final int GP = 6;
    private static final int PC = 7;

    // frame offsets
    private static final int OFP_FO = 0;
    private static final int RET_FO = -1;
    private static final int INIT_FO = -2;

    private int emitLoc = 0;
    private int highEmitLoc = 0;
    private int mainEntry = -1;
    private int globalOffset = 0;

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

        if (n.declarations != null) {
            n.declarations.accept(this, level);
        }

        if (mainEntry == -1) {
            throw new RuntimeException("Code generation error: missing main function");
        }

        emitComment("finale");
        emitRM("ST", FP, globalOffset + OFP_FO, FP, "push ofp");
        emitRM("LDA", FP, globalOffset, FP, "push frame");
        emitRM("LDA", AC, 1, PC, "load ac with return pointer");
        emitRMAbs("LDA", PC, mainEntry, "jump to main");
        emitRM("LD", FP, OFP_FO, FP, "pop frame");
        emitRO("HALT", 0, 0, 0, "done");
    }

    private void emitIORoutines() {
        emitComment("code for input routine");
        emitRM("ST", AC, RET_FO, FP, "store return");
        emitRO("IN", AC, 0, 0, "input");
        emitRM("LD", PC, RET_FO, FP, "return to caller");

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
        if ("main".equals(n.name)) {
            mainEntry = emitLoc;
        }

        emitComment("function " + n.name);
        emitRM("ST", AC, RET_FO, FP, "store return");

        if (n.body != null) {
            n.body.accept(this, level);
        }

        emitRM("LD", PC, RET_FO, FP, "return to caller");
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
        if (n.statements != null) {
            n.statements.accept(this, level);
        }
    }

    @Override
    public void visit(IfStmt n, int level) {
    }

    @Override
    public void visit(WhileStmt n, int level) {
    }

    @Override
    public void visit(ReturnStmt n, int level) {
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
        int tempOffset = INIT_FO - 1; // -3 -> temporary slot at fp-3 for simple arithmetic

        // evaluate left side → AC
        n.left.accept(this, level);

        // save left side
        emitRM("ST", AC, tempOffset, FP, "save left operand");

        // evaluate right side → AC
        n.right.accept(this, level);

        // restore left into AC1
        emitRM("LD", AC1, tempOffset, FP, "load left operand");

        // perform operation
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
                throw new RuntimeException("Code generation error: unsupported operator in OpExp");
        }
    }

    @Override
    public void visit(AssignExp n, int level) {
        if (!(n.lhs instanceof IdExp)) {
            throw new RuntimeException("Code generation error: only simple variable assignment supported so far");
        }

        IdExp lhs = (IdExp) n.lhs;
        Integer offset = globalVars.get(lhs.name);

        if (offset == null) {
            throw new RuntimeException("Code generation error: unknown variable '" + lhs.name + "'");
        }

        // generate code for rhs, result ends up in AC
        n.rhs.accept(this, level);

        // store result into global variable
        emitRM("ST", AC, offset, GP, "assign to " + lhs.name);
    }

    @Override
    public void visit(CallExp n, int level) {
    }

    @Override
    public void visit(IndexExp n, int level) {
    }

    @Override
    public void visit(IdExp n, int level) {
        Integer offset = globalVars.get(n.name);
        if (offset == null) {
            throw new RuntimeException("Code generation error: unknown variable '" + n.name + "'");
        }
        emitRM("LD", AC, offset, GP, "load variable " + n.name);
    }

    @Override
    public void visit(NumExp n, int level) {
        emitRM("LDC", AC, n.value, 0, "load const");
    }
}