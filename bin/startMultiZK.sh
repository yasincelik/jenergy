#!/bin/bash


DIR=`dirname $0`
pushd $DIR

#make a zookeeper config file
##it should store its data in ./data/zkhostname
		
python setupZKConfig.py	
while read p; do
	cat ./conf/zoo.$p.cfg
    #../lib/zookeeper/bin/zkServer-cygwin.sh start-foreground ./conf/zoo.cfg
	bash ../lib/zookeeper/bin/zkServer.sh start-foreground ./conf/zoo.$p.cfg&
    
done < ../conf/hosts

popd

