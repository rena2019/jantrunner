#!/bin/sh
mkdir -p lib
cp ../jantrunner.jar .
cp ../lib/*.* lib
java -jar jantrunner.jar 
