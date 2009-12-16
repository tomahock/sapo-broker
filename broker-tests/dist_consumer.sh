#!/bin/bash

cd `dirname $0`

consumers_start()
{
classpath="./conf:../sapo-broker/lib/*:./dist/*"


for consumerNum in $(seq $1 $2)
do

echo ""
consumer='consumer'$consumerNum
echo -n "$consumer"

java -server \
-Xverify:none -Xms16M -Xmx16M \
-Dapp=$consumer \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dfile.encoding=UTF-8 \
-cp $classpath \
pt.com.broker.performance.distributed.DistConsumerApp -h 127.0.0.1 -p 3323 -a $consumer &
#pt.com.broker.performance.distributed.DistConsumerApp -h 172.17.1.100 -p 3323 -a $consumer &


done

}

consumers_stop()
{
ps aux | grep DistConsumerApp | grep -v "grep" | awk '{print $2}' | xargs kill
}


consumers_default()
{
echo "Performance test consumers"
echo ""
echo "choose start or stop"
}
#
# Performance test consumers
#
case $1 in
'start')
consumers_start $2 $3
 ;;
'stop')
consumers_stop
 ;;
*)
consumers_default
 ;;
esac
