#!/bin/bash
DIR=`dirname $0`
pushd $DIR

java -cp "$(cygpath -pw "Jenergy.jar:conf")" edu.temple.cis.jenergy.apps.MasterRunner $@

popd
