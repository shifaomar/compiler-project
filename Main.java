/**
 * Main driver for C- compiler (Checkpoint 1).
 * Usage: CM -a <input.cm>   -- parse and output abstract syntax tree (.abs)
 *        CM <input.cm>       -- parse only
 */
import java.io.FileReader;
import java.io.PrintWriter;
import absyn.*;

class Main {
    public static void main(String[] argv) {
        try {
            if (argv.length == 0) {
                printUsage();
                System.exit(1);
            }

            boolean showTree = false;
            String path;

            if ("-a".equals(argv[0])) {
                if (argv.length < 2) {
                    printUsage();
                    System.exit(1);
                }
                showTree = true;
                path = argv[1];
            } else {
                if (argv.length > 1) {
                    printUsage();
                    System.exit(1);
                }
                path = argv[0];
            }

            Lexer lexer = new Lexer(new FileReader(path));
            parser p = new parser(lexer);
            Object result = p.parse().value;

            if (result == null) {
                System.err.println("Parse failed.");
                System.exit(1);
            }

            if (showTree) {
                // parser gives us the root of the ast
                Program program = (Program) result;
                // swap .cm for .abs in the output filename
                String outPath = path.replaceAll("\\.cm$", ".abs");
                if (outPath.equals(path)) outPath = path + ".abs";
                PrintWriter out = new PrintWriter(outPath);
                out.println("Abstract syntax tree for " + path + ":");
                out.println();
                // walk the tree and print it with nice indentation
                AbsynVisitor visitor = new ShowTreeVisitor(out);
                program.accept(visitor, 0);
                out.flush();
                out.close();
                System.out.println("Abstract syntax tree written to " + outPath);
            }

            if (!showTree) {
                System.out.println("Parse completed successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: CM -a <input.cm>");
        System.err.println("       CM <input.cm>");
        System.err.println("  -a  perform syntactic analysis and output abstract syntax tree (.abs)");
    }
}
