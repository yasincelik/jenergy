#!/bin/bash
die () {
        echo >&2 "$@"
            exit 1
        }

[ "$#" -eq 2 ] || die "hostname and format status required, $# provided"

echo "startLocal: ok "

DIR=`dirname $0`
pushd $DIR




currentHost=$1
format=$2
zoo=$3
echo 'FORMAT status: '$format

./stopAll.sh;
echo "stop all local: ok "

rm -r /tmp/data;
mkdir /tmp/data;



    
if [ $format == 'true' ]
then    
	./startCS.sh --format;
	rm pidFile
	echo "finished format ok "
    ln -s /tmp/data data; 
    #copy the jar and the conf files to have them in the same folder as the jar
    cp ../target/Jenergy-*-jar-with-dependencies.jar ./Jenergy.jar
    cp -r ../conf ./conf
    python setupSeeds.py  
fi

echo "config : ok "

echo "-----------------------------"
echo $currentHost
echo "-----------------------------"
./startCS.sh --ip $currentHost --path dir$currentHost &

echo "start CS local: ok"
sleep 2
./startZKPBS.sh $currentHost $format &

#./startZK.sh&

echo "start zk: ok"

sleep 5;
echo "Done starting the compute space"
popd


