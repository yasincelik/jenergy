#!/bin/sh


die () {
        echo >&2 "$@"
            exit 1
        }

[ "$#" -eq 2 ] || die "SIZE and G required: 2 argument required, $# provided"

echo "Starting Computation"

./runMaster.sh $@ &

sleep 5
./runWorker.sh &
./runWorker.sh &
./runWorker.sh &


