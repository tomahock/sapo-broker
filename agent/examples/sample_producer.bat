@echo off

title Sample Producer

chdir %CD%\..
set CLASSPATH=./conf
set CLASSPATH=%CLASSPATH%;./lib/*

set JAVA_OPTS= -Xms16M -Xmx16M
set JAVA_OPTS=%JAVA_OPTS% -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Dfile.encoding=UTF-8

@echo on

java %JAVA_OPTS% -cp "%CLASSPATH%" pt.com.broker.client.sample.Producer -n /test/foo -d TOPIC

pause
