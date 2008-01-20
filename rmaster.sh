#!/bin/sh
len=`expr length $0`
len=`expr $len - 11`
if [ $len -gt 1 ]
then
  path=`expr substr $0 1 $len`
else
  path=`pwd`
fi
gksudo "java -Djava.library.path=$path -jar $path/RemoteMaster.jar -h $path"
