#!/bin/sh
#PBS -l walltime=0:60:00
########################################### 
#PBS -N cass-test-14
############################################
#PBS -q normal
#PBS -l nodes=16:ppn=8
#PBS -o MPItestIB16.log
#PBS -j oe
#PBS -m abe
#PBS -V 
#PBS -M moutai10@gmail.com

module load openmpi

cd /home/tub51722/dev/jenergy-deploy/examples
echo $PATH
pwd
java -version

distTool setnodes;

cat ../conf/hosts
sleep 2

mpirun -np 16 --hostfile ../conf/hosts matTest2 1000;
 
mpirun -np 16 --hostfile ../conf/hosts matTest2 2000;

mpirun -np 16 --hostfile ../conf/hosts matTest2 3000;

mpirun -np 16 --hostfile ../conf/hosts matTest2 4000;

mpirun -np 16 --hostfile ../conf/hosts matTest2 5000;

mpirun -np 16 --hostfile ../conf/hosts matTest2 6000;


