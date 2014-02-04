DIR=`dirname $0`
pushd $DIR
echo 'Looking for the masters 60s:'
sleep 10;
echo 'Looking for the masters 50s:'
sleep 10;
echo 'Looking for the masters 40s:'
sleep 10;
echo 'Looking for the masters 30s:'
sleep 10;
echo 'Looking for the masters 20s:'
sleep 20;

COUNTER=`distTool status 2>/dev/null | grep Master | wc -l`
while [  $COUNTER -ne 0 ]; do
	sleep 1
    COUNTER=`distTool status 2>/dev/null | grep Master | wc -l`
    echo 'Job in progress: number of masters up is:' $COUNTER 
done
echo 'Job All done: masters: '$COUNTER
popd
