#!/bin/sh
#PBS -l walltime=0:10:00
########################################### 
#PBS -N cass-test-2
############################################
#PBS -q normal
#PBS -l nodes=10:ppn=8
#PBS -o Jenergytest.log
#PBS -j oe
#PBS -m abe
#PBS -V 
#PBS -M moutai10@gmail.com


echo $PATH
java -version


distTool setnodes; 
distTool deployPBS;

distTool restartCS;
distTool waitCS 2;	
distTool prun -m runMaster.sh 1000 100 -w runWorker.sh -p 2 -q 1 --output&
distTool waitMaster;

distTool stopCS;



