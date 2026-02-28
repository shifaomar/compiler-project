package absyn;

public class ArrayDecl extends Absyn {
    public Type type;
    public String name;
    public int size;

    public ArrayDecl(int row, int col, Type type, String name, int size) {
        super(row, col);
        this.type = type;
        this.name = name;
        this.size = size;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
