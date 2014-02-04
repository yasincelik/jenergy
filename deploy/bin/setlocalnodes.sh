DIR=`dirname $0`
echo 'current directory:'$DIR
pushd $DIR
echo 'nodes available are: '
echo 'using the local method'

rm ../conf/hosts 2>/dev/null
echo '127.0.0.1' >> ../conf/hosts


cat ../conf/hosts
popd
