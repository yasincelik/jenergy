#!/bin/bash
DIR=`dirname $0`
pushd $DIR
#get latest jar file and config file from source resource folder
DATADIR=/mnt/cassRam
if [ "$1" == "--format" ]; then

            echo "Removing previous launches data."
            if [ -d "conf" ]; then
                rm -r conf
            fi
            if [ -d "data" ]; then
            	rm -r data
            fi
            if [ -e "hosts" ]; then
                rm hosts
            fi	
	    exit
fi

#copy the jar and the conf files to have them in the same folder as the jar

#cp ../target/Jenergy-*-jar-with-dependencies.jar ./Jenergy.jar
#cp -r ../conf ./conf
#cp -r ../conf ./conf
#python setupSeeds.py

#java -Xms2000m -Xmx3000m -classpath Jenergy.jar edu.temple.cis.jenergy.computespace.ComputeSpaceManager $@

java -Dcom.sun.sdp.debug=~/debug.log -Dcom.sun.sdp.conf=/home/tue87589/ib.conf -Djava.net.preferIPv4Stack=true -classpath Jenergy.jar edu.temple.cis.jenergy.computespace.ComputeSpaceManager $@
sleep 1
#java -Xms4000m -Xmx6000m -classpath Jenergy.jar edu.temple.cis.jenergy.computespace.ComputeSpaceManager $@

popd

