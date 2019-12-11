src = jsonToolset
dep = fileToolset
jarPath = ../../lib/json/json-lib-2.4-jdk15.jar:../../lib/json/commons-beanutils-1.8.0.jar:../../lib/json/commons-collections-3.2.1.jar:../../lib/json/commons-lang-2.5.jar:../../lib/json/commons-logging-1.1.1.jar:../../lib/json/ezmorph-1.0.6.jar
srcClassPath = ./:../fileToolset/bin/:$(jarPath)
destClassPath = ./bin/

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java ../$(dep)/bin/$(dep).class $(destClassPath)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

../$(dep)/bin/$(dep).class: ../$(dep)/Makefile
	cd ../$(dep)/ && make

$(destClassPath):
	mkdir $(destClassPath)

clean:
	rm -rf $(destClassPath)

