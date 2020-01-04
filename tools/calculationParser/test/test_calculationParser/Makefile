target = calculationParser
src = test_$(target)
dep = ../../bin/calculationParser.class
srcClassPath = ./:../../bin/
destClassPath = ./

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) ../../bin/$(target).class
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(destClassPath):
	mkdir $(destClassPath)

../../bin/$(target).class: ../../Makefile
	cd ../.. && make all

clean:
	rm -rf $(destClassPath)$(src).class

run:
	make all
	java -cp $(srcClassPath) $(src)

