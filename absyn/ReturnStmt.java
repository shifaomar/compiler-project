package absyn;

public class ReturnStmt extends Stmt {
    public Exp expr;

    public ReturnStmt(int row, int col, Exp expr) {
        super(row, col);
        this.expr = expr;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
