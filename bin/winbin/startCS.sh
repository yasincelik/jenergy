#!/bin/bash
DIR=`dirname $0`
pushd $DIR
#get latest jar file and config file from source resource folder
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

cp ../target/Jenergy-*-jar-with-dependencies.jar ./Jenergy.jar
cp -r ../conf ./conf
python setupSeeds.py

java -classpath Jenergy.jar edu.temple.cis.jenergy.computespace.ComputeSpaceManager $@
popd

