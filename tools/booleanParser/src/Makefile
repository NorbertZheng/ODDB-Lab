src = booleanExecutor
dep = ../../../kernel/SQLParser/src/whereNode.java ../bin/booleanParser.class
srcClassPath = ./:../bin/:../../../kernel/SQLParser/src/
destClassPath = ./

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep) 
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(destClassPath):
	mkdir $(destClassPath)

../bin/booleanParser.class: ../Makefile
	cd .. && make all

clean:
	rm -rf *.class

run:
	make all

