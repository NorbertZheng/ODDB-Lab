src = Totem
dep = ./kernel/SQLParser/bin/SQLParser.class ./kernel/SQLExecutor/bin/SQLExecutor.class
jarPath = ./lib/json/json-lib-2.4-jdk15.jar:./lib/json/commons-beanutils-1.8.0.jar:./lib/json/commons-collections-3.2.1.jar:./lib/json/commons-lang-2.5.jar:./lib/json/commons-logging-1.1.1.jar:./lib/json/ezmorph-1.0.6.jar
srcClassPath = ./:$(jarPath):./kernel/SQLParser/:./kernel/SQLParser/src/:./kernel/SQLParser/bin/:./kernel/SQLExecutor/:./kernel/SQLExecutor/bin/:./kernel/dataStorer/bin/:./tools/virtualDisk/bin/:./tools/jsonToolset/bin/:./tools/fileToolset/bin/
destClassPath = ./

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

./kernel/SQLParser/bin/SQLParser.class: ./kernel/SQLParser/Makefile
	cd ./kernel/SQLParser/ && make all

./kernel/SQLExecutor/bin/SQLExecutor.class: ./kernel/SQLExecutor/Makefile
	cd ./kernel/SQLExecutor/ && make all

$(destClassPath):
	mkdir $(destClassPath)

clean:
	# rm -rf $(destClassPath)
	rm -rf *.class
	cd ./kernel/SQLParser/ && make clean
	cd ./kernel/SQLExecutor/ && make clean
	cd ./kernel/dataStorer/ && make clean
	cd ./tools/virtualDisk/ && make clean
	cd ./tools/jsonToolset/ && make clean
	cd ./tools/fileToolset/ && make clean

run:
	make all
	rm -rf ./data
	java -cp $(srcClassPath) $(src)

