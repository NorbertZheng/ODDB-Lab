src = dataStorer
dep = ../../tools/virtualDisk/bin/virtualDisk.class
srcClassPath = ./:./src/:../SQLParser/src/:../SQLParser/bin/:../../tools/virtualDisk/bin/
destClassPath = ./bin/

all: $(destClassPath)$(src).class

$(destClassPath)$(src).class: $(src).java $(destClassPath) $(dep)
	javac -cp $(srcClassPath) $(src).java -d $(destClassPath)

$(dep): ../../tools/virtualDisk/Makefile
	cd ../../tools/virtualDisk && make all

$(destClassPath):
	mkdir $(destClassPath)

clean:
	rm -rf $(destClassPath)

run:
	make all
	cd ./bin && java -cp $(srcClassPath) $(src) DROP CLASS STUDENT

