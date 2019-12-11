srcDir = ../../
srcFile = fileToolset
testFile = test_writeFile
srcClassPath = ./:$(srcDir)bin/

all: $(testFile).class

$(testFile).class: $(testFile).java $(srcDir)bin/$(srcFile).class
	javac -cp $(srcClassPath) $(testFile).java

$(srcDir)bin/$(srcFile).class: $(srcDir)Makefile
	cd $(srcDir) && make
	# pwd

clean:
	rm -rf *.class *.txt ./test/

run: all
	java -cp $(srcClassPath) $(testFile)

