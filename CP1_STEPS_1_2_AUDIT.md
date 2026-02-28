# Checkpoint 1 — Steps 1 & 2 Audit Report

**Scope:** Incremental steps (1) Scanner and (2) Grammar-only parser connected to scanner, per Checkpoint Description and Marking Scheme.

---

## Step 1: Scanner — ✅ COMPLETE

### Marking Scheme Criteria

| Criterion | Status | Evidence |
|-----------|--------|----------|
| **1. Major token types** | ✅ | `c.flex` includes: keywords (bool, else, if, int, return, void, while, true, false), symbols (+, -, *, /, <, <=, >, >=, ==, !=, &&, \|\|, =, ~, ; , ( ) [ ] { }), whitespace (skipped), identifiers, numbers, comments (/* */), invalid characters (catch-all `.` → LEXICAL ERROR) |
| **2. Row/column numbers** | ✅ | `%line` and `%column` in c.flex; `Symbol(type, yyline, yycolumn, value)` passes line/column to parser |
| **3. Using JFlex** | ✅ | `c.flex` is a JFlex spec; `jflex c.flex` generates `Lexer.java` |
| **Run fac.cm** | ✅ | `java -cp .:java-cup-11b.jar Main ../cp1/C1-Package/fac.cm` → "Parse completed successfully." |

### Minor Note
- Lexical error message uses 0-based `yyline`/`yycolumn`; parser `report_error` uses 1-based. Consider using `(yyline+1)` and `(yycolumn+1)` in the scanner’s error message for consistency.

---

## Step 2: Parser (Grammar Only, Connected to Scanner) — ✅ COMPLETE

### Marking Scheme Criteria

| Criterion | Status | Evidence |
|-----------|--------|----------|
| **1. Parsing w/o output** | ✅ | Grammar rules have no semantic actions; no AST building; `Main` prints only "Parse completed successfully." |
| **2. Using CUP** | ✅ | `c.cup` is a CUP spec; `java_cup.Main` generates `parser.java` and `sym.java` |
| **Run fac.cm, booltest.cm, gcd.cm, sort.cm, mutual.cm** | ✅ | All five parse successfully (verified) |

### Grammar Details
- **CUP precedence:** `nonassoc LT, LE, GT, GE, EQ, NE`; `left PLUS, MINUS`; `left STAR, SLASH`
- **Grammar rules:** No embedded code in productions; only a `parser code` block for `report_error`/`report_fatal_error` (error reporting, not AST)
- **C- coverage:** Declarations, params, statements (if/else/while/return/block/expression), expressions (relational, arithmetic, assign, call, index, parens, id, num)

### CUP Warnings (Non-blocking)
- Terminals NOT, AND, OR, TRUE, FALSE declared but unused — needed for step 3 (boolean expressions)
- 2 shift/reduce conflicts (if-else, assign) — resolved by CUP; standard for C-like grammars

---

## Verification Summary

| Test | Result |
|------|--------|
| fac.cm | Parse completed successfully |
| booltest.cm | Parse completed successfully |
| gcd.cm | Parse completed successfully |
| sort.cm | Parse completed successfully |
| mutual.cm | Parse completed successfully |

---

## Not Yet Done (Steps 3 & 4)

- **Step 3:** AST generation and `-a` option
- **Step 4:** Error recovery for multiple errors
- **Makefile:** Produces `Main.class`; checkpoint expects executable named `CM` (e.g. script or `CM` target)
- **Test programs:** Need 1.cm–5.cm in repo with comment headers
- **Report:** Project report not yet written

---

## Conclusion

**Steps 1 and 2 are complete** per the Checkpoint Description and Marking Scheme. The scanner uses JFlex with all required token types and line/column tracking, and the parser uses CUP with grammar-only rules and successfully parses all five sample C- programs.
