#!/bin/sh


die () {
        echo >&2 "$@"
            exit 1
        }

        
        
[ "$#" -eq 2 ] || die "SIZE and G required: 2 argument required, $# provided"

echo "Starting ComputeSpace..."
./startAllCSMultiZK.sh 1> /dev/null;
echo "Started ComputeSpace:"

echo "Starting Computation"

sleep 20
./runMaster.sh $@ &

sleep 5
./runWorker.sh &

sleep 20

SPID=`jps | grep QuorumPeerMain | awk '{print $1}' | head -n 1`

echo $SPID 

kill $SPID

printf "\n\n\n\n\n\n\n\n-------------------------------------\n Killed a palo queue replica with pid $SPID"
printf "\n\n\n\n\n\n\n\n-------------------------------------\n "








