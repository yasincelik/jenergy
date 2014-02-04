DIR=`dirname $0`
echo 'current directory:'$DIR
pushd $DIR
echo 'nodes available are: '
#cat `echo $PBS_NODEFILE`|uniq
#cat `echo $PBS_NODEFILE`|uniq > ../conf/hosts
#cat /var/spool/torque/aux/`qstat -u tub51722 | grep cass-test| awk '{print $1}'|tail -1`.nfs| uniq
#cat /var/spool/torque/aux/`qstat -u tub51722 | grep cass-test| awk '{print $1}'|tail -1`.nfs| uniq > ../conf/hosts


echo 'using the pbs method'

#echo 'using the following file for the hostnames: /var/spool/torque/aux/'`ls -ltr /var/spool/torque/aux/| grep owls |awk '{print $1}'| tail -1`

#cat /var/spool/torque/aux/`ls -r /var/spool/torque/aux/| grep owls | tail -1`|uniq


#cat /var/spool/torque/aux/`ls -ltr /var/spool/torque/aux/| grep owls |awk '{print $1}'| tail -1`|uniq > ../conf/hosts

echo $PBS_NODEFILE
cat `echo $PBS_NODEFILE` |uniq > ../conf/hosts

cat ../conf/hosts
popd
