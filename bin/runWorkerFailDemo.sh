#!/bin/sh


die () {
        echo >&2 "$@"
            exit 1
        }

        
        
[ "$#" -eq 2 ] || die "SIZE and G required: 2 argument required, $# provided"

echo "Starting ComputeSpace..."
./startAllCS.sh 1> /dev/null;
echo "Started ComputeSpace:"

echo "Starting Computation"

sleep 3
./runMaster.sh $@ &

sleep 5
./runWorker.sh &
./runWorker.sh &
./runWorker.sh &

sleep 20



WPID=`jps | grep WorkerRunner | awk '{print $1}' | head -n 1`

echo $WPID 



kill $WPID


printf "\n\n\n\n\n\n\n\n-------------------------------------\n Killed worker with pid $WPID"
printf "\n\n\n\n\n\n\n\n-------------------------------------\n "

#watch jps



