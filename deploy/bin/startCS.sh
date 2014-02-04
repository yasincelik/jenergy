#!/bin/bash

#IFS=$'\r\n' hostnames=($(cat /home/tue87589/jenergy/conf/hosts))

echo "------------------------------"
echo "ComputeSpaces are starting..."
echo "------------------------------"

filename='/home/tue87589/jenergy/conf/hosts'
hostnames=`cat $filename`
let "counter = 0"

for host in $hostnames; do
    toformat="false"
    if [ $counter -eq 0 ]
    then
        toformat="true"
    fi
    
    echo $host
   #echo $toformat
    #(ssh $host ~/jenergy/bin/startLocalCS.sh $host $toformat &) > /dev/null 2>&1
    nohup ssh $host ~/jenergy/bin/startLocalCS.sh $host $toformat &
    let "counter += 1"
    sleep 8
done
