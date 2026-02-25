package absyn;

import java.io.PrintWriter;

// walks the ast and prints it with indentation so you can see the structure
public class ShowTreeVisitor implements AbsynVisitor {
    private static final int SPACES = 4;
    private PrintWriter out;

    public ShowTreeVisitor(PrintWriter out) {
        this.out = out;
    }

    private void indent(int level) {
        for (int i = 0; i < level * SPACES; i++) out.print(" ");
    }

    private void println(String s) {
        out.println(s);
    }

    public void visit(Program n, int level) {
        indent(level);
        println("Program:");
        if (n.declarations != null) n.declarations.accept(this, level + 1);
    }

    public void visit(DeclList n, int level) {
        while (n != null) {
            if (n.head != null) n.head.accept(this, level);
            n = n.tail;
        }
    }

    public void visit(VarDecl n, int level) {
        indent(level);
        println("VarDecl: " + n.name);
        level++;
        if (n.type != null) n.type.accept(this, level);
    }

    public void visit(ArrayDecl n, int level) {
        indent(level);
        println("ArrayDecl: " + n.name + "[" + n.size + "]");
        level++;
        if (n.type != null) n.type.accept(this, level);
    }

    public void visit(FunDecl n, int level) {
        indent(level);
        println("FunDecl: " + n.name);
        level++;
        if (n.type != null) n.type.accept(this, level);
        if (n.params != null) n.params.accept(this, level);
        if (n.declarations != null) n.declarations.accept(this, level);
        if (n.body != null) n.body.accept(this, level);
    }

    public void visit(FunPrototype n, int level) {
        indent(level);
        println("FunPrototype: " + n.name);
        level++;
        if (n.type != null) n.type.accept(this, level);
        if (n.params != null) n.params.accept(this, level);
    }

    public void visit(ParamList n, int level) {
        while (n != null) {
            if (n.head != null) n.head.accept(this, level);
            n = n.tail;
        }
    }

    public void visit(Param n, int level) {
        indent(level);
        println("Param: " + n.name + (n.isArray ? "[]" : ""));
        if (n.type != null) n.type.accept(this, level + 1);
    }

    public void visit(Type n, int level) {
        indent(level);
        String name = n.typ == Type.INT ? "int" : n.typ == Type.VOID ? "void" : "bool";
        println("Type: " + name);
    }

    public void visit(StmtList n, int level) {
        while (n != null) {
            if (n.head != null) n.head.accept(this, level);
            n = n.tail;
        }
    }

    public void visit(CompoundStmt n, int level) {
        indent(level);
        println("CompoundStmt:");
        level++;
        if (n.declarations != null) n.declarations.accept(this, level);
        if (n.statements != null) n.statements.accept(this, level);
    }

    public void visit(IfStmt n, int level) {
        indent(level);
        println("IfStmt:");
        level++;
        if (n.test != null) n.test.accept(this, level);
        if (n.thenpart != null) n.thenpart.accept(this, level);
        if (n.elsepart != null) n.elsepart.accept(this, level);
    }

    public void visit(WhileStmt n, int level) {
        indent(level);
        println("WhileStmt:");
        level++;
        if (n.test != null) n.test.accept(this, level);
        if (n.body != null) n.body.accept(this, level);
    }

    public void visit(ReturnStmt n, int level) {
        indent(level);
        println("ReturnStmt:");
        if (n.expr != null) n.expr.accept(this, level + 1);
    }

    public void visit(ExpStmt n, int level) {
        indent(level);
        println("ExpStmt:");
        if (n.expr != null) n.expr.accept(this, level + 1);
    }

    public void visit(NullStmt n, int level) {
        indent(level);
        println("NullStmt");
    }

    public void visit(ExpList n, int level) {
        while (n != null) {
            if (n.head != null) n.head.accept(this, level);
            n = n.tail;
        }
    }

    public void visit(OpExp n, int level) {
        indent(level);
        String opStr = "";
        switch (n.op) {
            case OpExp.EQ: opStr = "=="; break;
            case OpExp.NE: opStr = "!="; break;
            case OpExp.LT: opStr = "<"; break;
            case OpExp.LE: opStr = "<="; break;
            case OpExp.GT: opStr = ">"; break;
            case OpExp.GE: opStr = ">="; break;
            case OpExp.PLUS: opStr = "+"; break;
            case OpExp.MINUS: opStr = "-"; break;
            case OpExp.STAR: opStr = "*"; break;
            case OpExp.SLASH: opStr = "/"; break;
            case OpExp.ASSIGN: opStr = "="; break;
            default: opStr = "?";
        }
        println("OpExp: " + opStr);
        level++;
        if (n.left != null) n.left.accept(this, level);
        if (n.right != null) n.right.accept(this, level);
    }

    public void visit(AssignExp n, int level) {
        indent(level);
        println("AssignExp:");
        level++;
        if (n.lhs != null) n.lhs.accept(this, level);
        if (n.rhs != null) n.rhs.accept(this, level);
    }

    public void visit(CallExp n, int level) {
        indent(level);
        println("CallExp: " + n.name);
        if (n.args != null) n.args.accept(this, level + 1);
    }

    public void visit(IndexExp n, int level) {
        indent(level);
        println("IndexExp:");
        level++;
        if (n.array != null) n.array.accept(this, level);
        if (n.index != null) n.index.accept(this, level);
    }

    public void visit(IdExp n, int level) {
        indent(level);
        println("IdExp: " + n.name);
    }

    public void visit(NumExp n, int level) {
        indent(level);
        println("NumExp: " + n.value);
    }
}
