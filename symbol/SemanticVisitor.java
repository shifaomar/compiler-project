package symbol;

import absyn.*;

public class SemanticVisitor implements AbsynVisitor {
    private SymbolTable table;
    private boolean hasErrors;
    private int currentFunctionReturnType;

    private static final int ERROR_TYPE = -1;
    private static final int ARRAY_TYPE = -2;

    public SemanticVisitor(SymbolTable table) {
        this.table = table;
        this.hasErrors = false;
        this.currentFunctionReturnType = ERROR_TYPE;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    private void reportError(int row, int col, String message) {
        System.err.println("Type error at line " + (row + 1) + ", column " + (col + 1) + ": " + message);
        hasErrors = true;
    }

    private boolean isAssignable(Exp e) {
        return (e instanceof IdExp) || (e instanceof IndexExp);
    }

    private int getExpType(Exp e) {
        if (e == null) return Type.VOID;

        if (e instanceof NumExp) {
            return Type.INT;
        }

        if (e instanceof IdExp) {
            IdExp id = (IdExp) e;
            TableEntry entry = table.lookup(id.name);
            if (entry == null) {
                reportError(id.row, id.col, "undefined identifier '" + id.name + "'");
                return ERROR_TYPE;
            }
            if (entry.kind == TableEntry.FUNC) {
                reportError(id.row, id.col, "'" + id.name + "' is a function, not a variable");
                return ERROR_TYPE;
            }
            if (entry.kind == TableEntry.ARRAY) {
                return ARRAY_TYPE;
            }
            return entry.type;
        }

        if (e instanceof IndexExp) {
            IndexExp idx = (IndexExp) e;

            if (!(idx.array instanceof IdExp)) {
                reportError(idx.row, idx.col, "indexed expression must be an array identifier");
                return ERROR_TYPE;
            }

            IdExp arrId = (IdExp) idx.array;
            TableEntry entry = table.lookup(arrId.name);

            if (entry == null) {
                reportError(arrId.row, arrId.col, "undefined array '" + arrId.name + "'");
                return ERROR_TYPE;
            }

            if (entry.kind != TableEntry.ARRAY) {
                reportError(arrId.row, arrId.col, "'" + arrId.name + "' is not an array");
                return ERROR_TYPE;
            }

            int indexType = getExpType(idx.index);
            if (indexType != ERROR_TYPE && indexType != Type.INT) {
                reportError(idx.index.row, idx.index.col, "array index must be int");
            }

            return entry.type;
        }

        if (e instanceof CallExp) {
            CallExp call = (CallExp) e;
            TableEntry entry = table.lookup(call.name);

            if (entry == null) {
                reportError(call.row, call.col, "undefined function '" + call.name + "'");
                return ERROR_TYPE;
            }

            if (entry.kind != TableEntry.FUNC) {
                reportError(call.row, call.col, "'" + call.name + "' is not a function");
                return ERROR_TYPE;
            }

            checkCallArguments(call, entry);
            return entry.type;
        }

        if (e instanceof AssignExp) {
            AssignExp a = (AssignExp) e;

            if (!isAssignable(a.lhs)) {
                reportError(a.lhs.row, a.lhs.col, "left side of assignment must be a variable or array element");
            }

            int lhsType = getExpType(a.lhs);
            int rhsType = getExpType(a.rhs);

            if (lhsType != ERROR_TYPE && rhsType != ERROR_TYPE && lhsType != rhsType) {
                reportError(a.row, a.col, "assignment type mismatch");
            }

            return lhsType;
        }

        if (e instanceof OpExp) {
            OpExp op = (OpExp) e;
            int leftType = getExpType(op.left);
            int rightType = getExpType(op.right);

            switch (op.op) {
                case OpExp.PLUS:
                case OpExp.MINUS:
                case OpExp.STAR:
                case OpExp.SLASH:
                    if (leftType != ERROR_TYPE && leftType != Type.INT) {
                        reportError(op.left.row, op.left.col, "left operand must be int");
                    }
                    if (rightType != ERROR_TYPE && rightType != Type.INT) {
                        reportError(op.right.row, op.right.col, "right operand must be int");
                    }
                    return Type.INT;

                case OpExp.LT:
                case OpExp.LE:
                case OpExp.GT:
                case OpExp.GE:
                    if (leftType != ERROR_TYPE && leftType != Type.INT) {
                        reportError(op.left.row, op.left.col, "left operand must be int");
                    }
                    if (rightType != ERROR_TYPE && rightType != Type.INT) {
                        reportError(op.right.row, op.right.col, "right operand must be int");
                    }
                    return Type.BOOL;

                case OpExp.EQ:
                case OpExp.NE:
                    if (leftType != ERROR_TYPE && rightType != ERROR_TYPE && leftType != rightType) {
                        reportError(op.row, op.col, "operands of equality operator must have same type");
                    }
                    return Type.BOOL;

                default:
                    reportError(op.row, op.col, "unknown operator");
                    return ERROR_TYPE;
            }
        }

        reportError(e.row, e.col, "unknown expression kind");
        return ERROR_TYPE;
    }

    private void checkCallArguments(CallExp call, TableEntry funcEntry) {
        ParamList params = funcEntry.params;
        ExpList args = call.args;

        int pos = 1;

        while (params != null && args != null) {
            int argType = getExpType(args.head);
            int paramType = params.head.type.typ;

            if (argType != ERROR_TYPE && argType != paramType) {
                reportError(args.head.row, args.head.col,
                    "argument " + pos + " of function '" + call.name + "' has wrong type");
            }

            if (params.head.isArray) {
                if (!(args.head instanceof IdExp)) {
                    reportError(args.head.row, args.head.col,
                        "argument " + pos + " of function '" + call.name + "' must be an array");
                } else {
                    IdExp id = (IdExp) args.head;
                    TableEntry argEntry = table.lookup(id.name);
                    if (argEntry != null && argEntry.kind != TableEntry.ARRAY) {
                        reportError(args.head.row, args.head.col,
                            "argument " + pos + " of function '" + call.name + "' must be an array");
                    }
                }
            }

            params = params.tail;
            args = args.tail;
            pos++;
        }

        if (params != null || args != null) {
            reportError(call.row, call.col, "wrong number of arguments in call to '" + call.name + "'");
        }
    }

    public void visit(Program n, int level) {
        table.scopePush();

        table.insert("input", new TableEntry("input", Type.INT, null, 0, 0));

        Param outputParam = new Param(0, 0, new Type(0, 0, Type.INT), "x", false);
        ParamList outputParams = new ParamList(0, 0, outputParam, null);
        table.insert("output", new TableEntry("output", Type.VOID, outputParams, 0, 0));

        if (n.declarations != null) {
            n.declarations.accept(this, level);
        }
    }

    public void visit(DeclList n, int level) {
        while (n != null) {
            if (n.head != null) {
                n.head.accept(this, level);
            }
            n = n.tail;
        }
    }

    public void visit(VarDecl n, int level) {
        TableEntry entry = new TableEntry(TableEntry.VAR, n.name, n.type.typ, n.row, n.col);
        if (!table.insert(n.name, entry)) {
            reportError(n.row, n.col, "redefinition of variable '" + n.name + "'");
        }
    }

    public void visit(ArrayDecl n, int level) {
        TableEntry entry = new TableEntry(n.name, n.type.typ, n.size, n.row, n.col);
        if (!table.insert(n.name, entry)) {
            reportError(n.row, n.col, "redefinition of array '" + n.name + "'");
        }
    }

    public void visit(FunPrototype n, int level) {
        TableEntry entry = new TableEntry(n.name, n.type.typ, n.params, n.row, n.col);
        if (!table.insert(n.name, entry)) {
            reportError(n.row, n.col, "redefinition of function '" + n.name + "'");
        }
    }

    public void visit(FunDecl n, int level) {
        TableEntry funcEntry = new TableEntry(n.name, n.type.typ, n.params, n.row, n.col);
        if (!table.insert(n.name, funcEntry)) {
            reportError(n.row, n.col, "redefinition of function '" + n.name + "'");
        }

        table.scopePush();

        int oldReturnType = currentFunctionReturnType;
        currentFunctionReturnType = n.type.typ;

        if (n.params != null) {
            n.params.accept(this, level);
        }

        if (n.declarations != null) {
            n.declarations.accept(this, level);
        }

        if (n.body != null) {
            n.body.accept(this, level);
        }

        currentFunctionReturnType = oldReturnType;
        table.scopePop();
    }

    public void visit(ParamList n, int level) {
        while (n != null) {
            if (n.head != null) {
                n.head.accept(this, level);
            }
            n = n.tail;
        }
    }

    public void visit(Param n, int level) {
        TableEntry entry;
        if (n.isArray) {
            entry = new TableEntry(n.name, n.type.typ, 0, n.row, n.col);
        } else {
            entry = new TableEntry(TableEntry.VAR, n.name, n.type.typ, n.row, n.col);
        }

        if (!table.insert(n.name, entry)) {
            reportError(n.row, n.col, "redefinition of parameter '" + n.name + "'");
        }
    }

    public void visit(Type n, int level) {
    }

    public void visit(StmtList n, int level) {
        while (n != null) {
            if (n.head != null) {
                n.head.accept(this, level);
            }
            n = n.tail;
        }
    }

    public void visit(CompoundStmt n, int level) {
        table.scopePush();

        if (n.declarations != null) {
            n.declarations.accept(this, level);
        }

        if (n.statements != null) {
            n.statements.accept(this, level);
        }

        table.scopePop();
    }

    public void visit(IfStmt n, int level) {
        int testType = getExpType(n.test);
        if (testType != ERROR_TYPE && testType != Type.INT && testType != Type.BOOL) {
            reportError(n.test.row, n.test.col, "if test expression must be int or bool");
        }

        if (n.thenpart != null) {
            n.thenpart.accept(this, level);
        }

        if (n.elsepart != null) {
            n.elsepart.accept(this, level);
        }
    }

    public void visit(WhileStmt n, int level) {
        int testType = getExpType(n.test);
        if (testType != ERROR_TYPE && testType != Type.INT && testType != Type.BOOL) {
            reportError(n.test.row, n.test.col, "while test expression must be int or bool");
        }

        if (n.body != null) {
            n.body.accept(this, level);
        }
    }

    public void visit(ReturnStmt n, int level) {
        if (currentFunctionReturnType == Type.VOID) {
            if (n.expr != null) {
                reportError(n.row, n.col, "void function should not return a value");
            }
        } else {
            if (n.expr == null) {
                reportError(n.row, n.col, "non-void function must return a value");
            } else {
                int returnType = getExpType(n.expr);
                if (returnType != ERROR_TYPE && returnType != currentFunctionReturnType) {
                    reportError(n.row, n.col, "return type does not match function declaration");
                }
            }
        }
    }

    public void visit(ExpStmt n, int level) {
        if (n.expr != null) {
            getExpType(n.expr);
        }
    }

    public void visit(NullStmt n, int level) {
    }

    public void visit(ExpList n, int level) {
        while (n != null) {
            if (n.head != null) {
                getExpType(n.head);
            }
            n = n.tail;
        }
    }

    public void visit(OpExp n, int level) {
        getExpType(n);
    }

    public void visit(AssignExp n, int level) {
        getExpType(n);
    }

    public void visit(CallExp n, int level) {
        getExpType(n);
    }

    public void visit(IndexExp n, int level) {
        getExpType(n);
    }

    public void visit(IdExp n, int level) {
        getExpType(n);
    }

    public void visit(NumExp n, int level) {
        getExpType(n);
    }
}

