@echo off
if NOT _%KMJ_HOME% == _ goto homeset
SET KMJ_HOME=.
:homeset
java -cp %KMJ_HOME%;%KMJ_HOME%/classes com.hifiremote.jp1.RemoteMaster %1 %2 %3 %4 %5 %6 %7 %8 %9
