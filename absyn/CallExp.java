package absyn;

// function call: name(args)
public class CallExp extends Exp {
    public String name;
    public ExpList args;

    public CallExp(int row, int col, String name, ExpList args) {
        super(row, col);
        this.name = name;
        this.args = args;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
