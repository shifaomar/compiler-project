package absyn;

public class IdExp extends Exp {
    public String name;

    public IdExp(int row, int col, String name) {
        super(row, col);
        this.name = name;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
