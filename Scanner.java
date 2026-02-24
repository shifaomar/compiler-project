/**
 * Scanner wrapper for C- (similar to ssheik08_a1).
 * Wraps the JFlex-generated Lexer; use getNextToken() for the parser.
 * Run as main to test the scanner only (prints tokens to stdout).
 */
import java.io.InputStreamReader;
import java.io.FileReader;
import java_cup.runtime.Symbol;

public class Scanner {
    private Lexer lexer;

    public Scanner(Lexer lexer) {
        this.lexer = lexer;
    }

    /** Returns the next token (Symbol) for the parser. */
    public Symbol getNextToken() throws java.io.IOException {
        return lexer.next_token();
    }

    /** Test the scanner: read from stdin and print each token. */
    public static void main(String[] argv) {
        try {
            java.io.Reader in = argv.length > 0
                ? new FileReader(argv[0])
                : new InputStreamReader(System.in);
            Scanner scanner = new Scanner(new Lexer(in));
            Symbol tok;
            while ((tok = scanner.getNextToken()) != null) {
                if (tok.sym < sym.terminalNames.length)
                    System.out.print(sym.terminalNames[tok.sym]);
                else
                    System.out.print("#" + tok.sym);
                if (tok.value != null)
                    System.out.print("(" + tok.value + ")");
                System.out.println();
            }
        } catch (Exception e) {
            System.err.println("Unexpected exception:");
            e.printStackTrace();
        }
    }
}
