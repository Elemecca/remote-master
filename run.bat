@echo off
if NOT _%KMJ_HOME% == _ goto homeset
SET KMJ_HOME=.
:homeset
java -cp %KMJ_HOME%;%KMJ_HOME%/classes com.hifiremote.jp1.KeyMapMaster
