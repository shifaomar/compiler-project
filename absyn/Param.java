package absyn;

public class Param extends Absyn {
    public Type type;
    public String name;
    public boolean isArray;

    public Param(int row, int col, Type type, String name, boolean isArray) {
        super(row, col);
        this.type = type;
        this.name = name;
        this.isArray = isArray;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
