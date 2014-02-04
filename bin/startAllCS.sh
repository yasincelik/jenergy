#!/bin/bash
DIR=`dirname $0`
pushd $DIR
./stopAll.sh;

./startCS.sh --format;

rm pidFile

#copy the jar and the conf files to have them in the same folder as the jar
cp ../target/Jenergy-*-jar-with-dependencies.jar ./Jenergy.jar
cp -r ../conf ./conf
python setupSeeds.py

while read p; do
    ./startCS.sh --ip $p --path dir$p&
    echo $! >> pidFile;
    sleep 5;
    #echo `jps | awk '{print $1}'`>>pidFile;
	
done < ../conf/hosts


./startZK.sh&
sleep 5;

#sleep 5;
echo "Done starting the compute space"
popd


