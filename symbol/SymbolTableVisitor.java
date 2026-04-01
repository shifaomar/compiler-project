package symbol;

import java.io.PrintWriter;
import absyn.*;

// walks ast, builds symbol table, checks undefined/redefined, optionally dumps for -s
public class SymbolTableVisitor implements AbsynVisitor {
    private SymbolTable table;
    private PrintWriter symOut;
    private boolean showSymbolTable;
    private boolean hasErrors;

    public SymbolTableVisitor(SymbolTable table) {
        this(table, null, false);
    }

    public SymbolTableVisitor(SymbolTable table, PrintWriter symOut, boolean showSymbolTable) {
        this.table = table;
        this.symOut = symOut;
        this.showSymbolTable = showSymbolTable;
        this.hasErrors = false;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    // report to stderr, track that we had errors
    private void reportError(int row, int col, String message) {
        System.err.println("Error at line " + (row + 1) + ", column " + (col + 1) + ": " + message);
        hasErrors = true;
    }

    // only prints when -s was used
    private void symPrint(String s) {
        if (showSymbolTable && symOut != null) {
            symOut.println(s);
        }
    }

    private boolean sameParams(ParamList a, ParamList b) {
        while (a != null && b != null) {
            if (a.head == null || b.head == null) return a.head == b.head;
            if (a.head.type.typ != b.head.type.typ) return false;
            if (a.head.isArray != b.head.isArray) return false;
            a = a.tail;
            b = b.tail;
        }
        return a == null && b == null;
    }

    private boolean sameFunctionSignature(TableEntry e, int returnType, ParamList params) {
        if (e == null || e.kind != TableEntry.FUNC) return false;
        if (e.type != returnType) return false;
        return sameParams(e.params, params);
    }

    public void visit(Program n, int level) {
        table.scopePush();

        // built-ins
        table.insert("input", new TableEntry("input", absyn.Type.INT, null, 0, 0));
        table.insert("output", new TableEntry("output", absyn.Type.VOID, null, 0, 0));

        if (n.declarations != null) {
            n.declarations.accept(this, level);
        }

        if (showSymbolTable) {
            symPrint("");
            symPrint("=== global scope ===");
            symPrint(table.formatCurrentScope(1));
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
        TableEntry e = new TableEntry(TableEntry.VAR, n.name, n.type.typ, n.row, n.col);
        if (!table.insert(n.name, e)) {
            reportError(n.row, n.col, "redefinition of variable '" + n.name + "'");
        }
    }

    public void visit(ArrayDecl n, int level) {
        TableEntry e = new TableEntry(n.name, n.type.typ, n.size, n.row, n.col);
        if (!table.insert(n.name, e)) {
            reportError(n.row, n.col, "redefinition of array '" + n.name + "'");
        }
    }

    public void visit(FunDecl n, int level) {
        TableEntry existing = table.lookup(n.name);

        if (existing == null) {
            TableEntry funcEntry = new TableEntry(n.name, n.type.typ, n.params, n.row, n.col);
            funcEntry.isPrototype = false;
            table.insert(n.name, funcEntry);
        } else {
            if (existing.kind != TableEntry.FUNC) {
                reportError(n.row, n.col, "'" + n.name + "' already exists and is not a function");
            } else if (existing.isPrototype) {
                if (!sameFunctionSignature(existing, n.type.typ, n.params)) {
                    reportError(n.row, n.col, "definition of function '" + n.name + "' does not match its prototype");
                } else {
                    existing.isPrototype = false; // upgrade prototype to definition
                }
            } else {
                reportError(n.row, n.col, "redefinition of function '" + n.name + "'");
            }
        }

        table.scopePush();

        // params go in new scope
        if (n.params != null) {
            n.params.accept(this, level);
        }

        // locals
        if (n.declarations != null) {
            n.declarations.accept(this, level);
        }

        if (showSymbolTable) {
            int d = table.getDepth();
            String pad = "  ".repeat(d - 1);
            symPrint("");
            symPrint(pad + "=== Entering function " + n.name + " ===");
            symPrint(table.formatCurrentScope(d));
        }

        if (n.body != null) {
            n.body.accept(this, level);
        }

        if (showSymbolTable) {
            int d = table.getDepth();
            String pad = "  ".repeat(d - 1);
            symPrint(pad + "=== Exiting function " + n.name + " ===");
            symPrint(table.formatCurrentScope(d));
        }

        table.scopePop();
    }

    public void visit(FunPrototype n, int level) {
        TableEntry existing = table.lookup(n.name);

        if (existing == null) {
            TableEntry e = new TableEntry(n.name, n.type.typ, n.params, n.row, n.col);
            e.isPrototype = true;
            table.insert(n.name, e);
            return;
        }

        if (existing.kind != TableEntry.FUNC) {
            reportError(n.row, n.col, "'" + n.name + "' already exists and is not a function");
            return;
        }

        if (!sameFunctionSignature(existing, n.type.typ, n.params)) {
            reportError(n.row, n.col, "conflicting declaration of function '" + n.name + "'");
        }

        // matching prototype or matching existing definition: do nothing
    }

    public void visit(ParamList n, int level) {
        while (n != null && n.head != null) {
            n.head.accept(this, level);
            n = n.tail;
        }
    }

    public void visit(Param n, int level) {
        int kind = n.isArray ? TableEntry.ARRAY : TableEntry.VAR;
        TableEntry e;
        if (n.isArray) {
            e = new TableEntry(n.name, n.type.typ, 0, n.row, n.col);
        } else {
            e = new TableEntry(kind, n.name, n.type.typ, n.row, n.col);
        }
        if (!table.insert(n.name, e)) {
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

        if (showSymbolTable && !table.formatCurrentScope().trim().isEmpty()) {
            int d = table.getDepth();
            String pad = "  ".repeat(d - 1);
            symPrint(pad + "=== Entering block ===");
            symPrint(table.formatCurrentScope(d));
        }

        if (n.statements != null) {
            n.statements.accept(this, level);
        }

        if (showSymbolTable && !table.formatCurrentScope().trim().isEmpty()) {
            int d = table.getDepth();
            String pad = "  ".repeat(d - 1);
            symPrint(pad + "=== Exiting block ===");
            symPrint(table.formatCurrentScope(d));
        }

        table.scopePop();
    }

    public void visit(IfStmt n, int level) {
        if (n.test != null) n.test.accept(this, level);
        if (n.thenpart != null) n.thenpart.accept(this, level);
        if (n.elsepart != null) n.elsepart.accept(this, level);
    }

    public void visit(WhileStmt n, int level) {
        if (n.test != null) n.test.accept(this, level);
        if (n.body != null) n.body.accept(this, level);
    }

    public void visit(ReturnStmt n, int level) {
        if (n.expr != null) n.expr.accept(this, level);
    }

    public void visit(ExpStmt n, int level) {
        if (n.expr != null) n.expr.accept(this, level);
    }

    public void visit(NullStmt n, int level) {
    }

    public void visit(ExpList n, int level) {
        while (n != null) {
            if (n.head != null) n.head.accept(this, level);
            n = n.tail;
        }
    }

    public void visit(OpExp n, int level) {
        if (n.left != null) n.left.accept(this, level);
        if (n.right != null) n.right.accept(this, level);
    }

    public void visit(AssignExp n, int level) {
        if (n.lhs != null) n.lhs.accept(this, level);
        if (n.rhs != null) n.rhs.accept(this, level);
    }

    public void visit(CallExp n, int level) {
        if (table.lookup(n.name) == null) {
            reportError(n.row, n.col, "undefined function '" + n.name + "'");
        }
        if (n.args != null) n.args.accept(this, level);
    }

    public void visit(IndexExp n, int level) {
        // array base is id - visit triggers lookup
        if (n.array != null) n.array.accept(this, level);
        if (n.index != null) n.index.accept(this, level);
    }

    public void visit(IdExp n, int level) {
        TableEntry e = table.lookup(n.name);
        if (e == null) {
            reportError(n.row, n.col, "undefined variable '" + n.name + "'");
        }
    }

    public void visit(NumExp n, int level) {
        // nothing to do for literals
    }
}
