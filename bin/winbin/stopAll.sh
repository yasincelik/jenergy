#!/bin/bash

DIR=`dirname $0`
pushd $DIR
#terminate all the local java threads
JPID=`ps -s | grep java | awk '{print$1}'`
echo $JPID >> pidFile


while read p; do
      echo $p
      kill $p  > /dev/null 2>&1 
done < pidFile
popd


