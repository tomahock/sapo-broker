#!/bin/sh

cd $(dirname $0)
cd ..

classpath="./conf:./lib/*"

java -server \
-Xverify:none -Xms32M -Xmx256M \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dfile.encoding=UTF-8 \
-Dagent-config-path=./conf/agent_example.config \
-Dbroker-global-config-path=./conf/broker_global.config \
-cp $classpath \
pt.com.broker.Start
