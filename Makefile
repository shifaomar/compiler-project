# C- Scanner and Parser — Linux
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

all: CM

# CM is a script that runs the compiler
CM: Main.class
	@echo '#!/bin/sh' > CM
	@echo 'exec $(JAVA) $(CLASSPATH) Main "$$@"' >> CM
	@chmod +x CM

# Build order: CUP generates parser + sym; JFlex needs sym; then compile all (including absyn)
Main.class: parser.java sym.java Lexer.java Token.java Scanner.java Main.java absyn/*.java symbol/*.java
	$(JAVAC) $(CLASSPATH) absyn/*.java symbol/*.java *.java

Lexer.java: c.flex sym.java
	$(JFLEX) c.flex

parser.java sym.java: c.cup
	$(CUP) -expect 1 c.cup

# # Test scanner only (no parser): java Scanner [file.cm]
# # Test full parse: java Main file.cm
# run: all
# 	$(JAVA) $(CLASSPATH) Main ../C1-Package/fac.cm

clean:
	rm -f parser.java sym.java Lexer.java *.class CM




