src = booleanParser
dep = whereNode.java
srcClassPath = ./:./src/
destClassPath = ./bin/

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(src).java: $(src).jj
	javacc $(src).jj

$(destClassPath):
	mkdir $(destClassPath)

clean:
	rm -rf *.java
	rm -rf $(destClassPath)

run:
	make all
	cd ./bin && java -cp $(srcClassPath) $(src) 2 >= 1

