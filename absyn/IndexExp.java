package absyn;

public class IndexExp extends Exp {
    public Exp array;
    public Exp index;

    public IndexExp(int row, int col, Exp array, Exp index) {
        super(row, col);
        this.array = array;
        this.index = index;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
