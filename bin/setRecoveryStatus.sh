#!/bin/bash
DIR=`dirname $0`
pushd $DIR
cp ../target/Jenergy-*-jar-with-dependencies.jar ./Jenergy.jar
cp -r ../conf ./conf
java -cp "Jenergy.jar:conf" edu.temple.cis.jenergy.computespace.RecoveryManager $@

popd
