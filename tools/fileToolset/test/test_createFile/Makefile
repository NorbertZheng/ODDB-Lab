srcDir = ../../
srcFile = fileToolset
testFile = test_createFile
srcClassPath = ./:$(srcDir)bin/

all: test_createFile.class

test_createFile.class: $(testFile).java $(srcDir)bin/$(srcFile).class
	javac -cp $(srcClassPath) $(testFile).java

$(srcDir)bin/$(srcFile).class: $(srcDir)Makefile
	cd $(srcDir) && make
	# pwd

clean:
	rm -rf *.class *.txt ./test/

run: all
	java -cp $(srcClassPath) $(testFile)

