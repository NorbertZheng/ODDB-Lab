src = SQLExecutor
dep = ../SQLParser/bin/SQLParser.class
srcClassPath = ./:./src/:../SQLParser/src/:../SQLParser/bin/
destClassPath = ./bin/

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(dep): ../SQLParser/Makefile
	cd ../SQLParser && make all

$(destClassPath):
	mkdir $(destClassPath)

clean:
	rm -rf *.java
	rm -rf $(destClassPath)

run:
	make all
	cd ./bin && java -cp $(srcClassPath) $(src) DROP CLASS STUDENT

