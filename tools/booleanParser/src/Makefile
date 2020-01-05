src = booleanExecutor
dep = whereNode.java ../bin/booleanParser.class
srcClassPath = ./:../bin/
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

