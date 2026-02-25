# C- Compiler (Checkpoint 1)

A compiler for the C- language. Right now it can scan C- source, parse it, and build an abstract syntax tree. The tree gets printed to a `.abs` file so you can see the structure of your program.

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

You need to pass `-a` and a C- file:

```bash
./CM -a myfile.cm
```

That parses `myfile.cm` and writes the abstract syntax tree to `myfile.abs` in the same directory. If you run `CM` without `-a`, it will just print usage.

You can also run it directly with Java:

```bash
java -cp .:java-cup-11b.jar Main -a myfile.cm
```

---

## Testing

We’ve tried it on the five sample programs from the C1-Package: `fac.cm`, `booltest.cm`, `gcd.cm`, `sort.cm`, and `mutual.cm`. They all parse and produce valid ASTs.

---

## Project layout

- `c.flex` — scanner (JFlex)
- `c.cup` — parser and AST building (CUP)
- `absyn/` — AST node classes
- `Main.java` — entry point and `-a` handling
