#!/bin/bash
DIR=`dirname $0`
pushd $DIR

#java -Xms1000m -Xmx2000m -cp "Jenergy.jar:conf" edu.temple.cis.jenergy.apps.MasterRunner $@

java -Dcom.sun.sdp.debug=debugM.log -Dcom.sun.sdp.conf=/home/tue87589/ib.conf -Djava.net.preferIPv4Stack=true -cp "Jenergy.jar:conf"  -Xms1000m -Xmx4000m edu.temple.cis.jenergy.apps.MasterRunner $@ >~/tmp/masterOutput

echo $@
#java -cp "Jenergy.jar:conf"  -Xms1000m -Xmx4000m edu.temple.cis.jenergy.apps.MasterRunner $@ >~/tmp/masterOutput
popd
