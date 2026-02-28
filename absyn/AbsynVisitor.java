package absyn;

// visitor interface so we can walk the ast (e.g. to print it or later for type checking)
public interface AbsynVisitor {
    void visit(Program n, int level);
    void visit(DeclList n, int level);
    void visit(VarDecl n, int level);
    void visit(ArrayDecl n, int level);
    void visit(FunDecl n, int level);
    void visit(FunPrototype n, int level);
    void visit(ParamList n, int level);
    void visit(Param n, int level);
    void visit(Type n, int level);
    void visit(StmtList n, int level);
    void visit(CompoundStmt n, int level);
    void visit(IfStmt n, int level);
    void visit(WhileStmt n, int level);
    void visit(ReturnStmt n, int level);
    void visit(ExpStmt n, int level);
    void visit(NullStmt n, int level);
    void visit(ExpList n, int level);
    void visit(OpExp n, int level);
    void visit(AssignExp n, int level);
    void visit(CallExp n, int level);
    void visit(IndexExp n, int level);
    void visit(IdExp n, int level);
    void visit(NumExp n, int level);
}
