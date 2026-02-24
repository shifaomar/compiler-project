/**
 * Main driver for C- parser (Checkpoint 1).
 * Connects the scanner to the parser and runs the parse.
 * Usage: java -cp .;cup.jar Main <input.c>
 */
import java.io.FileReader;

class Main {
    public static void main(String[] argv) {
        try {
            String path = argv.length > 0 ? argv[0] : null;
            if (path == null) {
                System.err.println("Usage: java Main <input.c or input.cm>");
                System.exit(1);
            }
            Lexer lexer = new Lexer(new FileReader(path));
            parser p = new parser(lexer);
            p.parse();
            System.out.println("Parse completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
