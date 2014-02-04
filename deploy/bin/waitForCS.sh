#!/bin/bash
die () {
        echo >&2 "$@"
            exit 1
        }

[ "$#" -eq 2 ] || die "Number of CSs to wait for not provided"



DIR=`dirname $0`
pushd $DIR
WAITINGTIME=0
COUNTER=`distTool status 2>/dev/null | grep ComputeSpace | wc -l`
NUMNODES=$2
echo 'number of nodes to check: '$NUMNODES


while [ $COUNTER -lt $NUMNODES ]; do
	sleep 1
	let WAITINGTIME=$WAITINGTIME+1
    COUNTER=`distTool status 2>/dev/null | grep ComputeSpace | wc -l`
    echo 'The number of CSs up is: '$COUNTER
    echo 'Waited for: '$WAITINGTIME's'
    if [ $WAITINGTIME -gt 200 ]; then 
  		echo "WarnCS:CS not fully stable: but moving on with existing CSs"
  		COUNTER=$NUMNODES
	fi
    
     
done
echo 'All done waiting: the number of CSs up is:' $COUNTER
popd
