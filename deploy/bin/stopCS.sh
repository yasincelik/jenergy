filename='/home/tue87589/jenergy/conf/hosts'
flines=`cat $filename`
echo "Jobs are being killed..."
for line in $flines; do
    echo $line
    ssh $line kill  `ssh $line "jps | grep 'ComputeSpaceManager\|QuorumPeerMain\|Worker\|Master' | cut -c1-5"`
    sleep 1
#ssh $line kill  `ssh $line "/opt/tools/java-1.6.0_u25/bin/jps | grep QuorumPeerMain | cut -c1-5"`
#ssh $line kill  `ssh $line "/opt/tools/java-1.6.0_u25/bin/jps | grep Master | cut -c1-5"`
done
echo "Done!"
