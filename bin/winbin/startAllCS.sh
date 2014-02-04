#!/bin/bash
DIR=`dirname $0`
pushd $DIR
./stopAll.sh;

./startCS.sh --format;
rm pidFile


while read p; do
    ./startCS.sh --ip $p --path dir$p&
    echo $! >> pidFile;
    sleep 5;
    echo `ps -s | grep java | awk '{print$1}'`>>pidFile;
done < ../conf/hosts


./startZK.sh&
sleep 5;

sleep 5;
echo "Done starting the compute space"
popd


