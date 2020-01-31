src = calculationExecutor
dep = ../../../kernel/SQLParser/src/calculationNode.java ../bin/calculationParser.class
srcClassPath = ./:../bin/:../../../kernel/SQLParser/src/
destClassPath = ./

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep) 
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(destClassPath):
	mkdir $(destClassPath)

../bin/calculationParser.class: ../Makefile
	cd .. && make all

clean:
	rm -rf *.class

run:
	make all

