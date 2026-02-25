package absyn;

public class FunPrototype extends Absyn {
    public Type type;
    public String name;
    public ParamList params;

    public FunPrototype(int row, int col, Type type, String name, ParamList params) {
        super(row, col);
        this.type = type;
        this.name = name;
        this.params = params;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
