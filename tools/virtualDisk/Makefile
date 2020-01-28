src = virtualDisk
dep = $(src).java ../fileToolset/bin/fileToolset.class ../jsonToolset/bin/jsonToolset.class
jarPath = ../../lib/json/json-lib-2.4-jdk15.jar:../../lib/json/commons-beanutils-1.8.0.jar:../../lib/json/commons-collections-3.2.1.jar:../../lib/json/commons-lang-2.5.jar:../../lib/json/commons-logging-1.1.1.jar:../../lib/json/ezmorph-1.0.6.jar
srcClassPath = ./:$(srcDir)bin/:$(jarPath):../fileToolset/bin/:../jsonToolset/bin/:./src/:../../kernel/SQLParser/src/
destClassPath = ./bin/

all: $(destClassPath)$(src).class

# src class
$(destClassPath)$(src).class: $(dep) $(destClassPath)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

# dep class
../fileToolset/bin/fileToolset.class: ../fileToolset/Makefile
	cd ../fileToolset/ && make

../jsonToolset/bin/jsonToolset.class: ../jsonToolset/Makefile
	cd ../jsonToolset/ && make

$(destClassPath):
	mkdir $(destClassPath)

clean:
	rm -rf $(destClassPath)
