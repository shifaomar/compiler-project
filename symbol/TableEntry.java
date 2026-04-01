package symbol;

import absyn.ParamList;

// one symbol table entry: variable, array, or function
public class TableEntry {
    public static final int VAR = 0;
    public static final int ARRAY = 1;
    public static final int FUNC = 2;

    public int kind;      // VAR, ARRAY, or FUNC
    public String name;   // key in the hash table
    public int type;      // int, bool, or void
    public int arraySize; // only for arrays
    public ParamList params;  // only for functions, null if void
    public int row;
    public int col;
    public boolean isPrototype;

    // simple variable
    public TableEntry(int kind, String name, int type, int row, int col) {
        this.kind = kind;
        this.name = name;
        this.type = type;
        this.arraySize = 0;
        this.params = null;
        this.row = row;
        this.col = col;
        this.isPrototype = false;
    }

    // array variable
    public TableEntry(String name, int type, int arraySize, int row, int col) {
        this.kind = ARRAY;
        this.name = name;
        this.type = type;
        this.arraySize = arraySize;
        this.params = null;
        this.row = row;
        this.col = col;
        this.isPrototype = false;
    }

    // function
    public TableEntry(String name, int returnType, ParamList params, int row, int col) {
        this.kind = FUNC;
        this.name = name;
        this.type = returnType;
        this.arraySize = 0;
        this.params = params;
        this.row = row;
        this.col = col;
        this.isPrototype = false;
    }

    // turn type code into "int", "void", or "bool"
    public static String typeToString(int t) {
        switch (t) {
            case 0: return "int";
            case 1: return "void";
            case 2: return "bool";
            default: return "?";
        }
    }

    // e.g. "(int, bool)" or "(void)"
    public String paramsToString() {
        if (params == null) return "(void)";
        StringBuilder sb = new StringBuilder("(");
        ParamList p = params;
        boolean first = true;
        while (p != null && p.head != null) {
            if (!first) sb.append(", ");
            sb.append(typeToString(p.head.type.typ));
            if (p.head.isArray) sb.append("[]");
            first = false;
            p = p.tail;
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        switch (kind) {
            case VAR:
                return name + ": " + typeToString(type);
            case ARRAY:
                return name + "[" + arraySize + "]: " + typeToString(type);
            case FUNC:
                return name + ": function " + typeToString(type) + " " + paramsToString();
            default:
                return name + ": ?";
        }
    }
}
