del /Q classes\com\hifiremote\jp1\*
del /Q classes\com\hifiremote\jp1\io\*
if NOT exist num-master.txt goto go
javac -deprecation -Xlint:unchecked -d classes -classpath .;classes UpdateDigitMapsBin.java
java -cp classes UpdateDigitMapsBin
del num-master.txt
:go
javac %1 %2 -deprecation -Xlint:unchecked -d classes -classpath .;classes; com/hifiremote/decodeir/*.java com/hifiremote/jp1/*.java com/hifiremote/jp1/io/*.java
javac -deprecation -Xlint:unchecked -d classes -classpath .;classes Setup.java

