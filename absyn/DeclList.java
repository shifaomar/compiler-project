package absyn;

public class DeclList extends Absyn {
    public Absyn head;
    public DeclList tail;

    public DeclList(int row, int col, Absyn head, DeclList tail) {
        super(row, col);
        this.head = head;
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
