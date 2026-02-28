package absyn;

public class ParamList extends Absyn {
    public Param head;
    public ParamList tail;

    public ParamList(int row, int col, Param head, ParamList tail) {
        super(row, col);
        this.head = head;
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
