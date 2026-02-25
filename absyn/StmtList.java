package absyn;

public class StmtList extends Absyn {
    public Stmt head;
    public StmtList tail;

    public StmtList(int row, int col, Stmt head, StmtList tail) {
        super(row, col);
        this.head = head;
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
