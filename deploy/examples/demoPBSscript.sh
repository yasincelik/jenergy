#!/bin/sh
#PBS -l walltime=0:30:00
########################################### 
#PBS -N cass-test-6
############################################
#PBS -q normal
#PBS -l nodes=10:ppn=8
#PBS -o Jenergytest4.log 
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
distTool waitCS 10;
distTool prun -m runMaster.sh 1000 100 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 2000 200 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 3000 300 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 4000 400 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 5000 500 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 6000 600 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 7000 700 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 8000 800 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 9000 900 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;

distTool restartCS&
distTool waitCS 10;
distTool prun -m runMaster.sh 10000 1000 -w runWorker.sh -p 1 -q 1 --output&
distTool waitMaster;
distTool stopCS;



