#!/bin/bash
DIR=`dirname $0`
pushd $DIR

currenthost=$1
firstzkflag=$2

python setupZKConfig.py $currenthost $firstzkflag;


bash ../lib/zookeeper/bin/zkServer.sh start ./conf/zoo.$currenthost.cfg &

popd
