#!/bin/bash

# start agents
#./broker1.sh  >> broker1.log &
#./broker2.sh >> broker2.log &

cd `dirname $0`

classpath="./conf:../sapo-broker/lib/*:./dist/*:../clients/java-component/dist/*:../acl/dbauth/java/dist/*"


#wait for 5 seconds
#echo "Giving time (5s) for the agents to start..."
#sleep 5s


#init tests
java -server \
-Xverify:none -Xms16M -Xmx16M \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dfile.encoding=UTF-8 \
-Dconfig-file=./conf/testparams.xml \
-cp $classpath \
pt.com.broker.functests.Main -a 1

# all
#pt.com.broker.functests.Main -a 1
# all with 5 runs each test
#pt.com.broker.functests.Main -n 1 -r 5
#just positive tests
#pt.com.broker.functests.Main -p 1
# just negative tests
#pt.com.broker.functests.Main -n 1
# just postive Topic tests
#pt.com.broker.functests.Main -t 1
# just positive Queue tests
#pt.com.broker.functests.Main -q 1
# just positive Virtual Queue tests
#pt.com.broker.functests.Main -v 1
# just positive SSL and Authentication relatated tests
#pt.com.broker.functests.Main -s 1
# just positive UDP tests
#pt.com.broker.functests.Main -u 1

ps aux | grep BROKER_TEST | grep -v "grep" | awk '{print $2}' | xargs kill

