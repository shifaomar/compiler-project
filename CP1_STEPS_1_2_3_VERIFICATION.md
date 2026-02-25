# Checkpoint 1 — Steps 1, 2, 3 Verification

**Scope:** Incremental steps (1) Scanner, (2) Grammar-only parser, (3) AST generation + `-a` option.

---

## Checkpoint Description Checklist

| Requirement | Status |
|-------------|--------|
| **(1) Scanner** for C- language | ✅ c.flex, JFlex |
| **(2) Parser** with grammar rules only, connected to scanner | ✅ c.cup, CUP |
| **(3) Embedded code** to produce syntax trees | ✅ c.cup semantic actions |
| **-a option** — output abstract syntax tree | ✅ Main.java |
| **Hierarchical structure** clearly in output | ✅ Indented tree in .abs |
| **Grammar** simplified with CUP precedence/associativity | ✅ precedence directives |
| **Makefile** — `make` produces CM | ✅ |
| **Makefile** — `make clean` removes generated files | ✅ |
| **README** — build-and-test instructions | ✅ |
| **README** — acknowledge C1-Package | ✅ |
| **No options given** — output usage | ✅ |

---

## Marking Scheme Checklist

### Scanner (20)
| Criterion | Status |
|-----------|--------|
| Major token types: keywords, symbols, whitespace, identifiers, numbers, comments, invalid chars | ✅ |
| Row/column numbers | ✅ %line %column, 1-based in errors |
| Must use JFlex | ✅ c.flex |
| Run fac.cm | ✅ |

### Parser (40)
| Criterion | Status |
|-----------|--------|
| Parse w/o output | ✅ (legacy mode removed; -a required) |
| Generate abstract syntax trees | ✅ |
| Must use CUP | ✅ c.cup |
| Run fac.cm, booltest.cm, gcd.cm, sort.cm, mutual.cm | ✅ All 5 produce .abs |
| Tree displayed after completely built | ✅ Written to .abs file |
| Check *.cup file | ✅ |

### Error Recovery (20) — Step 4, not yet done
| Criterion | Status |
|-----------|--------|
| Basic / Major / Extensive | ⏳ Step 4 |

---

## Verification Results

All 5 programs produce valid ASTs:
- fac.cm ✓
- booltest.cm ✓
- gcd.cm ✓
- sort.cm ✓
- mutual.cm ✓

---

## Conclusion

**Steps 1, 2, and 3 are complete** per the Checkpoint Description and Marking Scheme.
