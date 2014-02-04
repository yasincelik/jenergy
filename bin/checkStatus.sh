#!/bin/bash

filename='/home/tue87589/jenergy/conf/hosts'
hostnames=`cat $filename`
let "counter = 0"

for host in $hostnames; do
    STATUS=`ssh $host "jps | grep 'ComputeSpaceManager' | cut -c1-5"`
    #STATUS=`ssh $host "jps | grep 'QuorumPeerMain' | cut -c1-5"`
     if [[ $STATUS -gt 0 ]]
    then
        echo $host" : ComputeSpaceManager successufully started."
    else
        echo $host" ComputeSpaceManager failed. Node is restarting..."
        toformat="false"
        if [ $counter -eq 0 ]
        then
            toformat="true"
        fi
        ssh $host kill  `ssh $host "jps | grep 'ComputeSpaceManager\|QuorumPeerMain\|Worker\|Master' | cut -c1-5"`
        sleep 5
        nohup ssh $host ~/jenergy/bin/startLocalCS.sh $host $toformat &
    fi
    let "counter += 1"
    sleep 1
done

#: <<'END'
hostnames=`cat $filename`
for host in $hostnames; do
    STATUS=`ssh $host "jps | grep 'QuorumPeerMain' | cut -c1-5"`
    if [[ $STATUS -gt 0 ]]
    then
        echo $host" : QuorumPeerMain successufully started."
    else
        echo $host" QuorumPeerMain failed. Node is restarting..."
        toformat="false"
        if [ $counter -eq 0 ]
        then
            toformat="true"
        fi
        ssh $host kill  `ssh $host "jps | grep 'ComputeSpaceManager\|QuorumPeerMain\|Worker\|Master' | cut -c1-5"`
        sleep 5
        nohup ssh $host ~/jenergy/bin/startLocalCS.sh $host $toformat &
    fi
    let "counter += 1"
    sleep 1
done

