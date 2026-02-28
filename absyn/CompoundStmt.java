package absyn;

public class CompoundStmt extends Stmt {
    public DeclList declarations;
    public StmtList statements;

    public CompoundStmt(int row, int col, DeclList declarations, StmtList statements) {
        super(row, col);
        this.declarations = declarations;
        this.statements = statements;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
