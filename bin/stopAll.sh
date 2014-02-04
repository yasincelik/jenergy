#!/bin/bash

DIR=`dirname $0`
pushd $DIR
#source ~/.bashrc
#echo $PATH
#which java
#java -XX:+PrintFlagsFinal
#exit 0
#terminate all the local java threads
jps | grep ComputeSpaceManager

JPID=`jps | grep ComputeSpaceManager | awk '{print $1}'`
echo $JPID >> pidFile


jps | grep Quorum
JPID=`jps | grep Quorum | awk '{print $1}'`
echo $JPID >> pidFile

jps | grep RecoveryManager
JPID=`jps | grep Quorum | awk '{print $1}'`
echo $JPID >> pidFile


jps | grep Worker
JPID=`jps | grep Worker | awk '{print $1}'`
echo $JPID >> pidFile

jps | grep Master
JPID=`jps | grep Master | awk '{print $1}'`
echo $JPID >> pidFile


#using ps x to be sure that the processes are shutdown 

JPID=`ps x | grep ComputeSpaceManager | awk '{print $1}'`
echo $JPID >> pidFile


#jps | grep Quorum
JPID=`ps x | grep zoo | awk '{print $1}'`
echo $JPID >> pidFile

#jps | grep RecoveryManager
JPID=`jps | grep RecoveryManager | awk '{print $1}'`
echo $JPID >> pidFile


jps | grep Worker
JPID=`ps x | grep Worker | awk '{print $1}'`
echo $JPID >> pidFile

jps | grep Master
JPID=`ps x | grep Master | awk '{print $1}'`
echo $JPID >> pidFile


while read p; do
      #echo $p
      kill $p  > /dev/null 2>&1 
done < pidFile
popd


