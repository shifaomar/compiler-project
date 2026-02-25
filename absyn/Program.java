package absyn;

// root of the ast; holds the list of top-level declarations
public class Program extends Absyn {
    public DeclList declarations;

    public Program(int row, int col, DeclList declarations) {
        super(row, col);
        this.declarations = declarations;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
