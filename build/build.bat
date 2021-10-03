cd ..
set CLASSPATH=".;./build/commons-cli-1.4.jar;./build/commons-math3-3.6.1.jar;"
javac ./cli/*.java ./ui/*.java ./util/*.java
jar -cfm ./build/MyProgram.jar ./build/manifest.txt ./cli/*.class ./ui/*.class ./util/*.class
xcopy /E /C /I .\resources .\build\resources

