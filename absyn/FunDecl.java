package absyn;

// a function definition: return type, name, params, local decls, and body
public class FunDecl extends Absyn {
    public Type type;
    public String name;
    public ParamList params;
    public DeclList declarations;
    public StmtList body;

    public FunDecl(int row, int col, Type type, String name, ParamList params,
                  DeclList declarations, StmtList body) {
        super(row, col);
        this.type = type;
        this.name = name;
        this.params = params;
        this.declarations = declarations;
        this.body = body;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
