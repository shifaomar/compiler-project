# C- Compiler

A compiler for the C- language. It can scan C- source, parse it, build an abstract syntax tree (AST), perform syntax error recovery, build symbol tables, and check for semantic errors (undefined/redefined identifiers). The tree gets printed to a `.abs` file and the symbol table to a `.sym` file when requested.

We used the Tiny sample parser from the C1-Package (CourseLink) as a reference when building this.

---

## What you need

- Java (we used OpenJDK 17)
- JFlex
- `java-cup-11b.jar` — put it in this folder (you can grab it from CourseLink or Maven Central)

---

## How to build

```bash
make
```

That compiles everything and creates the `CM` executable. If something fails, run `make clean` first and try again.

**On Windows** (if JFlex is at `C:\jflex`):

```bash
make JFLEX=C:/jflex/bin/jflex
```

---

## How to run

```bash
./CM myfile.cm           # parse and run symbol table (report errors only)
./CM -a myfile.cm        # also output abstract syntax tree to myfile.abs
./CM -s myfile.cm        # also output symbol table to myfile.sym
./CM -a -s myfile.cm     # output both (when valid)
```

- `-a` writes the abstract syntax tree to `myfile.abs`
- `-s` writes the symbol table (with entry/exit per scope) to `myfile.sym`
- Output files are only written when the input is valid (no syntax or semantic errors)

You can also run it directly with Java:

```bash
java -cp .:java-cup-11b.jar Main -a -s myfile.cm
```


---

## Testing

We’ve tried it on the five sample programs from the C1-Package: `fac.cm`, `booltest.cm`, `gcd.cm`, `sort.cm`, and `mutual.cm`. They all parse and produce valid ASTs.

For symbol table demo:
- `./CM -s ../cp1/C1-Package/gcd.cm` — symbol table at entry/exit for gcd
- `./CM -s symtab_demo.cm` — different kinds: int x; bool bbb[10]; void foo(void)
- `./CM undef_z.cm` — undefined variable 'z'
- `./CM redef_y.cm` — redefined variable 'y'

This submission includes custom test files `1.cm` through `5.cm`:

- `1.cm` parses without errors
- `2.cm`–`4.cm` contain targeted lexical/syntactic error cases (with no more than 3 errors each)
- `5.cm` contains multiple syntax errors to demonstrate error recovery across multiple constructs

Each test file includes a comment header describing the errors it contains and what aspect of the compiler it is testing.

The project was built and tested on the School of Computer Science Linux servers (`linux.socs.uoguelph.ca` environment).


---

## Project layout

- `c.flex` — scanner (JFlex)
- `c.cup` — parser, AST building (CUP), and error recovery
- `absyn/` — AST node classes
- `symbol/` — symbol table (TableEntry, SymbolTable, SymbolTableVisitor)
- `Main.java` — entry point, `-a` and `-s` handling


