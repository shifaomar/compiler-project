package absyn;

// lhs = rhs; lhs can be a variable or array index
public class AssignExp extends Exp {
    public Exp lhs;
    public Exp rhs;

    public AssignExp(int row, int col, Exp lhs, Exp rhs) {
        super(row, col);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
