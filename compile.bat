del /Q classes\com\hifiremote\jp1\*.class
if NOT exist num-master.txt goto go
javac -deprecation -Xlint:unchecked -d classes -classpath .;classes UpdateDigitMaps.java
java -cp classes UpdateDigitMaps
del num-master.txt
:go
javac %1 %2 -deprecation -Xlint:unchecked -d classes -classpath .;classes com/hifiremote/jp1/*.java
javac -deprecation -Xlint:unchecked -d classes -classpath .;classes Setup.java

