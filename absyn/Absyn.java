package absyn;

// base class for everything in the ast; row/col for error messages later
public abstract class Absyn {
    public int row, col;

    public Absyn(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public abstract void accept(AbsynVisitor visitor, int level);
}
