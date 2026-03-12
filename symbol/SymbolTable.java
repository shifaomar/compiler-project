package symbol;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

// hash table per scope, stack for nested scopes (functions/blocks)
public class SymbolTable {
    private ArrayList<HashMap<String, TableEntry>> scopeStack;

    public SymbolTable() {
        scopeStack = new ArrayList<>();
    }

    // push new scope
    public void scopePush() {
        scopeStack.add(new HashMap<String, TableEntry>(10));
    }

    // pop current scope
    public void scopePop() {
        if (!scopeStack.isEmpty()) {
            scopeStack.remove(scopeStack.size() - 1);
        }
    }

    // insert into current scope; false if redefined
    public boolean insert(String name, TableEntry entry) {
        if (scopeStack.isEmpty()) {
            throw new IllegalStateException("No scope: call scopePush() before insert");
        }
        HashMap<String, TableEntry> current = scopeStack.get(scopeStack.size() - 1);
        if (current.containsKey(name)) {
            return false; // redefined
        }
        current.put(name, entry);
        return true;
    }

    // search from innermost scope out; null if not found
    public TableEntry lookup(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            HashMap<String, TableEntry> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    // is name in current scope only?
    public boolean inCurrentScope(String name) {
        if (scopeStack.isEmpty()) return false;
        return scopeStack.get(scopeStack.size() - 1).containsKey(name);
    }

    // dump all scopes for display
    public String formatAllScopes() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scopeStack.size(); i++) {
            HashMap<String, TableEntry> scope = scopeStack.get(i);
            sb.append("  Scope ").append(i).append(":\n");
            Iterator<Map.Entry<String, TableEntry>> iter = scope.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, TableEntry> e = iter.next();
                sb.append("    ").append(e.getValue().toString()).append("\n");
            }
        }
        return sb.toString();
    }

    // current scope, indented by nesting level
    public String formatCurrentScope() {
        return formatCurrentScope(getDepth());
    }

    public String formatCurrentScope(int indentLevel) {
        if (scopeStack.isEmpty()) return "";
        int spaces = indentLevel * 2;  // 2 spaces per nesting level
        String pad = " ".repeat(Math.max(0, spaces));
        StringBuilder sb = new StringBuilder();
        HashMap<String, TableEntry> scope = scopeStack.get(scopeStack.size() - 1);
        Iterator<Map.Entry<String, TableEntry>> iter = scope.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, TableEntry> e = iter.next();
            sb.append(pad).append(e.getValue().toString()).append("\n");
        }
        return sb.toString();
    }

    public int getDepth() {
        return scopeStack.size();
    }

    public boolean isEmpty() {
        return scopeStack.isEmpty();
    }
}
