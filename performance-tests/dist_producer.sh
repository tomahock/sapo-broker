#!/bin/bash

cd `dirname $0`

producers_start()
{
classpath="./conf:../sapo-broker/lib/*:./dist/*"


for producerNum in $(seq $1 $2)
do

echo ""
producer='producer'$producerNum
echo -n "$producer"

java -server \
-Xverify:none -Xms16M -Xmx16M \
-Dapp=$producer \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dfile.encoding=UTF-8 \
-cp $classpath \
pt.com.broker.performance.distributed.DistProducerApp -h 127.0.0.1 -p 3323 -a $producer &
#pt.com.broker.performance.distributed.DistProducerApp -h 172.17.1.100 -p 3323 -a $producer &

done

}

producers_stop()
{
ps aux | grep DistProducerApp | grep -v "grep" | awk '{print $2}' | xargs kill
}


producers_default()
{
echo "Performance test producers"
echo ""
echo "choose start or stop"
}
#
# Performance test producers
#
case $1 in
'start')
producers_start $2 $3
 ;;
'stop')
producers_stop
 ;;
*)
producers_default
 ;;
esac
