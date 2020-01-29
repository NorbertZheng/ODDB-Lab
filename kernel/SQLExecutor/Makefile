src = SQLExecutor
dep = ../dataStorer/bin/dataStorer.class 
srcClassPath = ./:./src/:../SQLParser/src/:../dataStorer/src/:../dataStorer/bin/
destClassPath = ./bin/

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(dep): ../dataStorer/Makefile
	cd ../dataStorer && make all

$(destClassPath):
	mkdir $(destClassPath)

clean:
	rm -rf $(destClassPath)

run:
	make all
	cd ./bin && java -cp $(srcClassPath) $(src) DROP CLASS STUDENT

