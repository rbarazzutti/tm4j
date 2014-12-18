CC=gcc
JAVAC=javac
JAVAH=javah
JAVA=java

.SUFFIXES: .class .java
.PHONY: clean

all: Main.class Transaction.class libtm-tsx.so

.java.class:
	$(JAVAC) $<

libtm-tsx.so: Transaction.c Transaction.h
	$(CC) -Wall -Wextra -O3 -mrtm -I /usr/lib/jvm/java-7-openjdk-amd64/include/ -shared -fPIC -o $@ $<

Transaction.h: Transaction.class
	$(JAVAH) -jni Transaction

Main: Main.class
	$(JAVA) -Djava.library.path=. $@ 1

clean:
	$(RM) *.class libtm-tsx.so Transaction.h
