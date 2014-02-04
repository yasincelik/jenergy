#!/bin/sh
#PBS -l walltime=00:30:00
########################################### 
#PBS -N cass-test-18
############################################
#PBS -q normal
#PBS -l nodes=16:ppn=8
#PBS -o Jenergytest13.log 
#PBS -j oe 
#PBS -m abe
#PBS -V 
#PBS -M moutai10@gmail.com


echo $PATH
pwd
java -version
rm /tmp/masteroutput


distTool setnodes;
distTool deployPBS;


distTool restartCS&
distTool waitCS 5;
distTool prun -m runMaster.sh 1000 50 -w runWorker.sh -p 16 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 5;
distTool prun -m runMaster.sh 2000 125 -w runWorker.sh -p 16 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 5;
distTool prun -m runMaster.sh 3000 150 -w runWorker.sh -p 16 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 5;
distTool prun -m runMaster.sh 4000 250 -w runWorker.sh -p 16 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 5;
distTool prun -m runMaster.sh 5000 300 -w runWorker.sh -p 16 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 5;
distTool prun -m runMaster.sh 6000 375 -w runWorker.sh -p 16 -q 1 --output&
distTool waitMaster;
distTool stopCS;



