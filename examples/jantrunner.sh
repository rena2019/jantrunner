#!/bin/sh
mkdir -p lib
cp ../jantrunner.jar .
cp ../lib/*.* lib
java -cp /usr/share/ant/lib/*:jantrunner.jar:lib/* AntRunner