@echo off
mkdir lib
copy ..\jantrunner.jar .
copy ..\lib\*.* lib
java -cp %ANT_HOME%\lib\*;jantrunner.jar;lib\* AntRunner
