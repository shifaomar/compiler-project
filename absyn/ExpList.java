package absyn;

public class ExpList extends Absyn {
    public Exp head;
    public ExpList tail;

    public ExpList(int row, int col, Exp head, ExpList tail) {
        super(row, col);
        this.head = head;
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
