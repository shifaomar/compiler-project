/*
  C- Scanner (JFlex)
  Similar style to ssheik08_a1 + CUP compatibility like SampleParser tiny.flex.
  To build: run CUP first to generate sym.java, then: jflex c.flex
*/

/*import java_cup.runtime.*;*/
import java_cup.runtime.Symbol;

%%

%class Lexer

%eofval{
  return null;
%eofval};

%cup
%line
%column

%{
  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
ID = [_a-zA-Z][_a-zA-Z0-9]*
NUM = [0-9]+

%%

/* Keywords */
"bool" { return symbol(sym.BOOL); }
"else" { return symbol(sym.ELSE); }
"if" { return symbol(sym.IF); }
"int" { return symbol(sym.INT); }
"return" { return symbol(sym.RETURN); }
"void" { return symbol(sym.VOID); }
"while" { return symbol(sym.WHILE); }
"true" { return symbol(sym.TRUE); }
"false" { return symbol(sym.FALSE); }

/* Operators - two-char first */
"<=" { return symbol(sym.LE); }
">=" { return symbol(sym.GE); }
"==" { return symbol(sym.EQ); }
"!=" { return symbol(sym.NE); }
"&&" { return symbol(sym.AND); }
"||" { return symbol(sym.OR); }

/* Single-char tokens*/
"+" { return symbol(sym.PLUS); }
"-" { return symbol(sym.MINUS); }
"*" { return symbol(sym.STAR); }
"/" { return symbol(sym.SLASH); }
"<" { return symbol(sym.LT); }
">" { return symbol(sym.GT); }
"=" { return symbol(sym.ASSIGN); }
"~" { return symbol(sym.NOT); }

";" { return symbol(sym.SEMI); }
"," { return symbol(sym.COMMA); }
"(" { return symbol(sym.LPAREN); }
")" { return symbol(sym.RPAREN); }
"[" { return symbol(sym.LBRACK); }
"]" { return symbol(sym.RBRACK); }
"{" { return symbol(sym.LBRACE); }
"}" { return symbol(sym.RBRACE); }

/* Identifiers and numbers */

{ID} { return symbol(sym.ID, yytext()); }
{NUM} { return symbol(sym.NUM, Integer.valueOf(yytext())); } 

/* Whitespace */
{WhiteSpace}+   { /* skip */ }

/* Comments */
"/*" ([^*] | "*" [^/])* "*" "/"  { /* block comment */ }


/*Catch-all lexical error */
. {
	  System.err.println("LEXICAL ERROR: illegal character '" + yytext()
    + "' at " + (yyline + 1) + ":" + (yycolumn + 1));
    return symbol(sym.ERROR, yytext());
}

