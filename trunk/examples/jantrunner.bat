@echo off
mkdir lib
copy ..\jantrunner.jar .
copy ..\lib\*.* lib
java -jar jantrunner.jar 
