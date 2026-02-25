package absyn;

// binary op: left op right (arithmetic, comparison, assign)
public class OpExp extends Exp {
    public static final int EQ = 0;
    public static final int NE = 1;
    public static final int LT = 2;
    public static final int LE = 3;
    public static final int GT = 4;
    public static final int GE = 5;
    public static final int PLUS = 6;
    public static final int MINUS = 7;
    public static final int STAR = 8;
    public static final int SLASH = 9;
    public static final int ASSIGN = 10;

    public Exp left;
    public int op;
    public Exp right;

    public OpExp(int row, int col, Exp left, int op, Exp right) {
        super(row, col);
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
