#!/bin/bash

# start agents
./broker1.sh  >> broker1.log &
 ./broker2.sh >> broker2.log &

cd `dirname $0`

#wait for 5 seconds
echo "Giving time (5s) for the agents to start..."
#sleep 5s

classpath="./conf:../sapo-broker/lib/*:./dist/*"

#init tests
java -server \
-Xverify:none -Xms16M -Xmx16M \
-Dapp=SIMULATION_APP \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dconfig-file=./conf/testparams.xml \
-Dfile.encoding=UTF-8 \
-cp $classpath \
pt.com.broker.functests.simulation.MainAll

ps aux | grep BROKER_TEST | grep -v "grep" | awk '{print $2}' | xargs kill
