JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
.PHONY: clean jar permissions
	
sources = $(wildcard *.java)
classes = $(sources:.java=.class)

default: classes jar permissions 

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class *.jar

jar: Tunneling.class
	jar cvfe Tunneling.jar Tunneling *.class
	
permissions:
	chmod 755 Tunneling.jar
