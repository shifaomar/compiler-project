package absyn;

public class WhileStmt extends Stmt {
    public Exp test;
    public Stmt body;

    public WhileStmt(int row, int col, Exp test, Stmt body) {
        super(row, col);
        this.test = test;
        this.body = body;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
