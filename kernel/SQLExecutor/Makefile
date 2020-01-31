src = SQLExecutor
dep = ../dataStorer/bin/dataStorer.class ../../tools/calculationParser/bin/calculationParser.class ../../tools/calculationParser/src/bin/calculationExecutor.class ../../tools/booleanParser/bin/booleanParser.class ../../tools/booleanParser/src/bin/booleanExecutor.class
srcClassPath = ./:./src/:../SQLParser/src/:../dataStorer/src/:../dataStorer/bin/:../../tools/booleanParser/:../../tools/booleanParser/src/:../../tools/booleanParser/bin/:../../tools/calculationParser/:../../tools/calculationParser/src/../../tools/calculationParser/bin/
destClassPath = ./bin/

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

../dataStorer/bin/dataStorer.class: ../dataStorer/Makefile
	cd ../dataStorer && make all

../../tools/calculationParser/bin/calculationParser.class: ../../tools/calculationParser/Makefile
	cd ../../tools/calculationParser && make all

../../tools/calculationParser/src/bin/calculationExecutor.class: ../../tools/calculationParser/src/Makefile
	cd ../../tools/calculationParser/src && make all

../../tools/booleanParser/bin/booleanParser.class: ../../tools/booleanParser/Makefile
	cd ../../tools/booleanParser && make all

../../tools/booleanParser/src/bin/booleanExecutor.class: ../../tools/booleanParser/src/Makefile
	cd ../../tools/booleanParser/src && make all

$(destClassPath):
	mkdir $(destClassPath)

clean:
	rm -rf $(destClassPath)
	cd ../dataStorer && make clean
	cd ../../tools/calculationParser && make clean
	cd ../../tools/calculationParser/src && make clean
	cd ../../tools/booleanParser && make clean
	cd ../../tools/booleanParser/src && make clean

run:
	make all
	cd ./bin && java -cp $(srcClassPath) $(src) DROP CLASS STUDENT

