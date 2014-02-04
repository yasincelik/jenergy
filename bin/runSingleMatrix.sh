#!/bin/bash
DIR=`dirname $0`
pushd $DIR

java -cp "Jenergy.jar:conf" edu.temple.cis.jenergy.apps.Matrix $@

popd
