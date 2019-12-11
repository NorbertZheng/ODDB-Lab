srcDir = ../../
srcFile = jsonToolset
testFile = test_readJsonFile
jarPath = ../../../../lib/json/json-lib-2.4-jdk15.jar:../../../../lib/json/commons-beanutils-1.8.0.jar:../../../../lib/json/commons-collections-3.2.1.jar:../../../../lib/json/commons-lang-2.5.jar:../../../../lib/json/commons-logging-1.1.1.jar:../../../../lib/json/ezmorph-1.0.6.jar
srcClassPath = ./:$(srcDir)bin/:../../../fileToolset/bin/:$(jarPath)

all: $(testFile).class

$(testFile).class: $(testFile).java $(srcDir)bin/$(srcFile).class
	javac -cp $(srcClassPath) $(testFile).java

$(srcDir)bin/$(srcFile).class: $(srcDir)Makefile
	cd $(srcDir) && make
	# pwd

clean:
	rm -rf *.class *.txt

run: all
	java -cp $(srcClassPath) $(testFile)

