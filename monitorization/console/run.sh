#!/bin/bash

cd `dirname $0`

classpath="./conf:../lib/*:./dist/*:../../common-libs/*:../../comm-types/dist/*:../../bindings/protobuf/java/dist/*:../../bindings/thrift/java/dist/*:../../clients/java-component/dist/*"


java -server \
-Xverify:none -Xms32M -Xmx64M \
-Djava.awt.headless=true \
-Djava.net.preferIPv4Stack=true \
-Djava.net.preferIPv6Addresses=false \
-Dfile.encoding=UTF-8 \
-cp $classpath \
-Dcom.sun.management.jmxremote.port=3333 \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.authenticate=false \
pt.com.broker.monitorization.http.HttpMonitorizationServer -p 8878 -r ./wwwroot

echo ""
echo "Please, run ant (ant package) first."
echo ""
