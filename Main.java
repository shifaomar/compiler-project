/**
 * Main driver for C- compiler.
 * Usage: CM -a <input.cm>   -- output abstract syntax tree (.abs)
 *        CM -s <input.cm>   -- output symbol table (.sym)
 *        CM -a -s <input.cm> -- both
 *        CM <input.cm>      -- parse and run symbol table only
 */
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import absyn.*;
import symbol.*;

class Main {
    public static void main(String[] argv) {
        try {
            if (argv.length == 0) {
                printUsage();
                System.exit(1);
            }

            boolean showTree = false;
            boolean showSymTab = false;
            String path = null;

            int i = 0;
            while (i < argv.length) {
                if ("-a".equals(argv[i])) {
                    showTree = true;
                    i++;
                } else if ("-s".equals(argv[i])) {
                    showSymTab = true;
                    i++;
                } else if (argv[i].startsWith("-") && argv[i].length() > 1) {
                    // handle -as, -sa, etc.
                    for (int j = 1; j < argv[i].length(); j++) {
                        char c = argv[i].charAt(j);
                        if (c == 'a') showTree = true;
                        else if (c == 's') showSymTab = true;
                    }
                    i++;
                } else {
                    path = argv[i];
                    i++;
                    break;
                }
            }

            if (path == null || i < argv.length) {
                printUsage();
                System.exit(1);
            }

            Lexer lexer = new Lexer(new FileReader(path));
            parser p = new parser(lexer);
            Object result = p.parse().value;

            if (result == null) {
                System.err.println("Parse failed.");
                System.exit(1);
            }

            Program program = (Program) result;

            // always run symbol table (catches undefined/redefined)
            SymbolTable symTab = new SymbolTable();
            StringWriter symBuffer = new StringWriter();
            PrintWriter symOut = showSymTab ? new PrintWriter(symBuffer) : null;
            SymbolTableVisitor symVisitor = new SymbolTableVisitor(symTab, symOut, showSymTab);
            program.accept(symVisitor, 0);

            if (symVisitor.hasErrors()) {
                System.exit(1);
            }

            // semantic/type checking pass
            SymbolTable semTab = new SymbolTable();
            SemanticVisitor semVisitor = new SemanticVisitor(semTab);
            program.accept(semVisitor, 0);

            if (semVisitor.hasErrors()) {
                System.exit(1);
            }

            // only write when no syntax or semantic errors
            if (showSymTab) {
                String symPath = path.replaceAll("\\.cm$", ".sym");
                if (symPath.equals(path)) symPath = path + ".sym";
                PrintWriter f = new PrintWriter(symPath);
                f.print("Symbol table for " + path + "\n\n");
                f.print(symBuffer.toString());
                f.flush();
                f.close();
                System.out.println("Symbol table written to " + symPath);
            }

            if (showTree) {
                String outPath = path.replaceAll("\\.cm$", ".abs");
                if (outPath.equals(path)) outPath = path + ".abs";
                PrintWriter out = new PrintWriter(outPath);
                out.println("Abstract syntax tree for " + path + ":");
                out.println();
                AbsynVisitor visitor = new ShowTreeVisitor(out);
                program.accept(visitor, 0);
                out.flush();
                out.close();
                System.out.println("Abstract syntax tree written to " + outPath);
            }

            if (!showTree && !showSymTab) {
                System.out.println("Parse completed successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: CM [-a] [-s] <input.cm>");
        System.err.println("  -a  output abstract syntax tree (.abs)");
        System.err.println("  -s  output symbol table (.sym)");
    }
}
