#!/bin/bash
DIR=`dirname $0`
pushd $DIR
#../lib/zookeeper/bin/zkServer-cygwin.sh start-foreground ./conf/zoo.cfg
bash ../lib/zookeeper/bin/zkServer.sh start-foreground ./conf/zoo.cfg
popd
