if NOT exist num-master.txt goto go
javac -deprecation -d classes -classpath .;classes UpdateDigitMaps.java
java -cp classes UpdateDigitMaps
del num-master.txt
:go
javac %1 -deprecation -d classes -classpath .;classes com/hifiremote/jp1/*.java
javac -deprecation -d classes -classpath .;classes Setup.java

