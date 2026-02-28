# CIS4650 Checkpoint One — Project Report

**C- Compiler: Scanner, Parser, Abstract Syntax Tree, and Error Recovery**

---

## 1. What Has Been Done

For Checkpoint One we built a compiler for the C- language that follows the four incremental steps from the assignment. We started with a scanner, then added a grammar only parser and connected it to the scanner. After that we added the embedded code to build and display abstract syntax trees, and finally we incorporated error recovery so that multiple errors can be reported in a single run.

You run the compiler with `./CM -a file.cm` and it writes the abstract syntax tree to `file.abs`. We tested it on the five sample programs (fac.cm, booltest.cm, gcd.cm, sort.cm, mutual.cm) and they all parse correctly. The Makefile produces the CM executable, and the README explains how to build and run everything.

For error recovery, we added CUP recovery productions so the parser will continue after common syntax errors instead of stopping after the first one. Recovery was added for declaration-related errors (including a targeted missing semicolon case after declarations), statement sequences, parameter lists, and binary expressions with missing right operands. We verified this using the required custom test files 1.cm through 5.cm, including a multi-error file (5.cm) that reports several syntax errors in one run and still produces an AST file.

---

## 2. Techniques and Design

### 2.1 Scanner

We used JFlex to build the scanner. The spec lives in `c.flex` and defines all the token types that the marking scheme asks for. That includes keywords like bool, else, if, int, return, void, while, true, and false. It also covers symbols and operators such as +, -, *, /, <, <=, >, >=, ==, !=, &&, ||, =, and ~, plus the usual punctuation. We handle identifiers, numbers, whitespace (which we skip), block comments `/* ... */`, and a catch all rule for invalid characters that reports a lexical error.

Every token carries line and column information. We use JFlex’s `%line` and `%column` and pass `yyline` and `yycolumn` into the Symbol so the parser can report errors with the right position. When we hit an invalid character we print something like `LEXICAL ERROR: illegal character 'x' at 3:5`, and we use 1 based line and column so it matches what the parser does.

The scanner is set up to work with CUP. It uses `%cup` and returns `java_cup.runtime.Symbol` objects. We made sure to match two character operators like `<=` and `==` before the single character ones so we don’t tokenize them wrong.

### 2.2 Parser

We used CUP for the parser. The grammar is in `c.cup` and is based on the C- CFG from the C-Specification. Instead of adding extra grammar layers for precedence, we simplified things by using CUP’s precedence and associativity directives. For example, we have:

```
precedence right ASSIGN;
precedence nonassoc LT, LE, GT, GE, EQ, NE;
precedence left PLUS, MINUS;
precedence left STAR, SLASH;
```

That makes assignment right associative and gives relational operators lower precedence than arithmetic, which matches how C does it. The grammar covers declarations (variables, arrays, functions, and prototypes), parameters, statements (compound blocks, if/else, while, return, expression statements, and null statements), and expressions (binary ops, assignment, function calls, array indexing, identifiers, and numbers).

When the parser hits a syntax error it reports it to stderr with the line and column. We use the Symbol’s `left` and `right` fields and convert them to 1 based, so the messages look like `Error in line 5, column 12 : unexpected token`.

### 2.3 Abstract Syntax Tree

Each grammar rule has a semantic action that builds an AST node as we parse. The root of the tree is a `Program` node that holds the list of top level declarations. We have node types for all the different kinds of declarations (variables, arrays, functions, prototypes), for parameters and types, for statements (compound, if, while, return, expression, null), and for expressions (binary ops, assignment, calls, array indexing, identifiers, numbers).

We use a visitor pattern to walk the tree and print it. The `AbsynVisitor` interface and `ShowTreeVisitor` implementation let us traverse the AST and output it with indentation so you can see the structure. For `fac.cm`, the output looks like this:

```
Program:
    FunDecl: main
        Type: void
        VarDecl: fac
            Type: int
        VarDecl: x
            Type: int
        ...
        WhileStmt:
            OpExp: >
                IdExp: x
                NumExp: 1
            CompoundStmt:
                ...
```

When you pass the `-a` option, Main.java parses the file, gets the Program from the parser, and writes the tree to a `.abs` file with the same base name as the input.

### 2.4 Error Recovery

We implemented syntax error recovery in CUP using the special error symbol and a small set of targeted recovery productions. The goal was not to handle every possible malformed input, but to recover from common and obvious syntax mistakes so the parser can continue and report multiple errors in one run.

First, we improved parser error reporting by extending `report_error(...)` so messages include the line number, column number, token type, and token value (when present). This makes syntax errors easier to locate and understand when testing.

For recovery itself, we added productions in several parts of the grammar:

1. Declaration recovery: 
    A general fallback production (error SEMI) and a targeted case for missing semicolons after simple declarations (type_specifier ID error). The targeted case helps with common inputs such as int x followed by another declaration.

2. Statement sequence recovery: 
    Recovery in statement_list so an invalid statement can be skipped and later statements in the same block can still be parsed.

3. Parameter list recovery: 
    Recovery in param_list for malformed parameter lists (for example, an extra comma).

4. Expression recovery: 
    Targeted recovery for binary expressions with missing right operands (for example, x = 3 + ;).

We also adjusted declaration list construction so recovered invalid declarations (which return null) are skipped when building the AST list. This avoids inserting invalid nodes into the tree and allows later valid declarations to remain in the generated AST.

Testing shows the parser can recover with multiple syntax errors in a single file and continue parsing to generate a partial AST. 

For example: 
Our 5.cm test file includes a missing semicolon after a declaration, an invalid parameter list, and a missing right operand in an expression. The parser reports all of these errors in one run and writes the AST file. 

---

## 3. Lessons Learned

Getting the token names to match between JFlex and CUP took some care. We run CUP first to generate `sym.java`, and then we run JFlex, so the scanner can use the same terminal names. The order in the Makefile matters, and we had to get that right before everything would build.

Building lists in the right order was another thing we had to think about. For function arguments we have a rule like `arg_list ::= arg_list COMMA expression`, and we needed a helper `appendExpList` to add new arguments to the end of the list instead of ending up with them in reverse order.

Using CUP’s precedence directives instead of extra grammar layers made the grammar simpler and easier to work with. The if else and assignment conflicts are resolved by CUP in the usual way, so we didn’t have to worry about that ourselves.

Implementing error recovery in CUP taught us that where recovery rules are placed matters as much as the rules themselves. Some recovery points were straightforward (for example, malformed parameter lists and missing expression operands), but others were more difficult because overly broad or overlapping error productions can introduce extra CUP conflicts. Testing also showed that panic-mode recovery can skip more input than expected, so it was important to verify not only that errors were reported, but also that the parser acknowledged enought to produce a meaningful partial AST, without ignoring good code. 

---

## 4. Assumptions and Limitations

We assume the C-Specification grammar is the source of truth and that input files use `.cm` as the extension. We used the C1-Package Tiny sample parser as a reference and acknowledge it in the README.

There are a few limitations. The scanner doesn’t support line comments `//` yet. The boolean operators (&&, ||, ~) are in the scanner but we haven’t wired them into the expression grammar. Before error recovery, the parser would exit on the first fatal error; with error recovery it continues and reports multiple errors.

Our error recovery uses CUP’s panic-mode recovery with a limited set of synchronization points, mainly semicolons and commas. This works well for many common syntax mistakes, but it does not guarantee ideal recovery for every malformed input. Recovery is designed to be practical and reliable for common checkpoint-level errors (such as missing semicolons, malformed parameter lists, and incomplete binary expressions), but heavily corrupted input may still lead to incomplete recovery or termination near EOF.

---

## 5. Possible Improvements

There are several things we could improve. We could add `//` line comments to the scanner. We could wire up the boolean operators in expressions. We could add more recovery points for edge cases. We could make the error messages more helpful (for example, saying “expected semicolon” instead of just “syntax error”). We could also add a debug mode to show tokens during scanning.

Error recovery could be improved by adding more specialized recovery productions for additional syntactic contexts (for example, malformed blocks or parenthesized expressions) while still keeping the grammar conflict-free.


---

## 6. Contributions

**Shifa Sheikh:** Built the scanner (JFlex spec in c.flex with all token types and line/column tracking), the parser grammar (c.cup with grammar rules only, connected to the scanner), and the Makefile. Implemented incremental steps 1 and 2.

**Ayesha Khan:** Built the abstract syntax tree (absyn package, semantic actions in c.cup, ShowTreeVisitor), the `-a` option in Main.java, the README with build and run instructions, and this project report. Implemented incremental step 3.

**Athina Manolakou:** Implemented error recovery (error productions, multiple error reporting), created the five test programs (1.cm through 5.cm) with comment headers, and handled the remaining integration. Implemented incremental step 4.
