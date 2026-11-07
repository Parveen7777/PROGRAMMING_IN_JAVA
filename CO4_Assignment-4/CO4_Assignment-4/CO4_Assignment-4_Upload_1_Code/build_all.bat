@echo off
REM Compiles every task into build\<task> (JDK 17+ required)
mkdir build\t1 build\t2 build\t3 build\t4 build\t5 build\t5v1 build\t5v2 build\t6 2>nul
javac -d build\t1 Task01\*.java
javac -d build\t2 Task02\*.java
javac -d build\t3 Task03\*.java
javac -d build\t4 Task04\*.java
javac -d build\t5 Task05\Rates.java Task05\Converter.java Task05\ConverterSharedAction.java
javac -d build\t5v1 Task05\v1\*.java
javac -d build\t5v2 Task05\v2\*.java
dir /s /b Task06\src\*.java > build\t6sources.txt
javac -d build\t6 @build\t6sources.txt
echo All tasks compiled.
java -cp build\t1 FeeServiceTest
