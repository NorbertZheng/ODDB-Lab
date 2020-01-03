srcDir = ../../
dep = $(testFile).java ../../../fileToolset/bin/fileToolset.class ../../../jsonToolset/bin/jsonToolset.class
srcFile = virtualDisk
testFile = test_virtualDisk
jarPath = ../../../../lib/json/json-lib-2.4-jdk15.jar:../../../../lib/json/commons-beanutils-1.8.0.jar:../../../../lib/json/commons-collections-3.2.1.jar:../../../../lib/json/commons-lang-2.5.jar:../../../../lib/json/commons-logging-1.1.1.jar:../../../../lib/json/ezmorph-1.0.6.jar
srcClassPath = ./:$(srcDir)bin/:$(jarPath):../../../fileToolset/bin/:../../../jsonToolset/bin/

all: $(testFile).class

# src class
$(testFile).class: $(dep) $(srcDir)bin/$(srcFile).class 
	javac -cp $(srcClassPath) $(testFile).java

# dep class
$(srcDir)bin/$(srcFile).class: $(srcDir)Makefile
	cd $(srcDir) && make
	# pwd

../../../fileToolset/bin/fileToolset.class: ../../../fileToolset/Makefile
	cd ../../../fileToolset/ && make

../../../jsonToolset/bin/jsonToolset.class: ../../../jsonToolset/Makefile
	cd ../../../jsonToolset/ && make

clean:
	rm -rf *.class *.txt

run: all
	java -cp $(srcClassPath) $(testFile)

