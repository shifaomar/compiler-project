# C- Scanner and Parser (Checkpoint 1) — Linux
# Put java-cup-11b.jar in this directory, or set CUPJAR to its path.
#
# Windows local dev (JFlex at C:\jflex):
#   make JFLEX=C:/jflex/bin/jflex

JAVA   = java
JAVAC  = javac
JFLEX  ?= jflex
CUPJAR = java-cup-11b.jar
CLASSPATH = -cp .:$(CUPJAR)
CUP = $(JAVA) $(CLASSPATH) java_cup.Main

all: Main.class

# Build order: CUP generates parser + sym; JFlex needs sym; then compile all
Main.class: parser.java sym.java Lexer.java Token.java Scanner.java Main.java
	$(JAVAC) $(CLASSPATH) *.java

Lexer.java: c.flex sym.java
	$(JFLEX) c.flex

parser.java sym.java: c.cup
	$(CUP) -expect 40 c.cup

# # Test scanner only (no parser): java Scanner [file.cm]
# # Test full parse: java Main file.cm
# run: all
# 	$(JAVA) $(CLASSPATH) Main ../C1-Package/fac.cm

clean:
	rm -f parser.java sym.java Lexer.java *.class




