#!/bin/bash

# start agents
#./broker1.sh  >> broker1.log &
#./broker2.sh >> broker2.log &

cd `dirname $0`

# check version
java -version 2>&1 | grep 1.5 > /dev/null
if [ $? = 0 ] ; then # Yup, 1.5 still
  echo Found Java version 1.5
  classpath="./conf"

  for i in ./jvm15/lib/*.jar; do
    classpath=$classpath:$i
  done

  for i in ./lib/*.jar; do
    classpath=$classpath:$i
  done
else # we assume 1.6 here
  echo Found Java version 1.6
  classpath="./conf:../sapo-broker/lib/*:./dist/*"
fi

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
pt.com.broker.performance.Main

