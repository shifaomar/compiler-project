package absyn;

public class IfStmt extends Stmt {
    public Exp test;
    public Stmt thenpart;
    public Stmt elsepart;

    public IfStmt(int row, int col, Exp test, Stmt thenpart, Stmt elsepart) {
        super(row, col);
        this.test = test;
        this.thenpart = thenpart;
        this.elsepart = elsepart;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
