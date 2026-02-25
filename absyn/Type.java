package absyn;

public class Type extends Absyn {
    public static final int INT = 0;
    public static final int VOID = 1;
    public static final int BOOL = 2;

    public int typ;

    public Type(int row, int col, int typ) {
        super(row, col);
        this.typ = typ;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
