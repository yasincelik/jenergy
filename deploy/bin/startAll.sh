#!/bin/bash


IFS=$'\r\n' hostnames=($(cat /home/tue87589/jenergy/conf/hosts))

let "a = $1"

master="${hostnames[0]}"
echo "master "$master
ssh $master ~/jenergy/bin/runMaster.sh $3 $4 &
#~/runMaster.sh $3 $4
echo "1 = "$1
echo "2 = "$2
echo "3 = "$3
echo "4 = "$4
echo "5 = "$5
echo "6 = "$6
echo "7 = "$7
echo "8 = "$8
echo "9 = "$9
echo "10 = "${10}
echo "11 = "${11}
echo "----"
filename='/home/tue87589/jenergy/conf/hosts'
x=$(cat /home/tue87589/jenergy/conf/hosts | wc -l)
flines=`cat $filename`
let "counter = 0"
echo "Number of Host :"+$x
sleep 1
for ((i=0; i<$8; i++))
do
    host="${hostnames[$counter]}"
    echo "Worker is sent = "$host;
    nohup ssh $host  ~/jenergy/bin/runWorker.sh $host &
    sleep 0.5
    let "counter += 1"
    if [ $counter -eq $x ]
    then
        counter=0
    fi
done
#sleep 10
#ssh $master ~/jenergy/bin/runMaster.sh $3 $4 &
