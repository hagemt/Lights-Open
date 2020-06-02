.PHONY: default clean

JAVA_SRCS ?= $(shell find src -name '*.java')
JAVA_HOME ?= $(shell /usr/libexec/java_home -v 1.8)

default: lo.jar
	"$(JAVA_HOME)/bin/java" -jar $< state/state1.los

clean:
	git clean -dix

lo.jar: manifest.txt $(JAVA_SRCS:.java=.class)
	"$(JAVA_HOME)/bin/jar" cfm $@ $< -C src .

%.class: %.java
	"$(JAVA_HOME)/bin/javac" -sourcepath src $<
