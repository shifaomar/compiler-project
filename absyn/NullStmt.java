package absyn;

public class NullStmt extends Stmt {
    public NullStmt(int row, int col) {
        super(row, col);
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
