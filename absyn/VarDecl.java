package absyn;

public class VarDecl extends Absyn {
    public Type type;
    public String name;

    public VarDecl(int row, int col, Type type, String name) {
        super(row, col);
        this.type = type;
        this.name = name;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
