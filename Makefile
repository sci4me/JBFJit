SHELL=/bin/bash

all: clean
	mkdir bin
	shopt -s globstar && \
	javac -cp lib/*.jar -d bin **/*.java

clean: 
	rm -rf bin

run: all
	java -cp bin:lib/* com/sci/jbfjit/Main progs/hanoi.bf