target = calculationExecutor
src = test_$(target)
dep = ../../src/$(target).class
srcClassPath = ./:../../src/:../../bin/
destClassPath = ./

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(destClassPath):
	mkdir $(destClassPath)

$(dep): ../../src/Makefile
	cd ../../src/ && make all

clean:
	rm -rf $(destClassPath)$(src).class

run:
	make all
	java -cp $(srcClassPath) $(src)

