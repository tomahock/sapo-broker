@echo off

title broker1

chdir %CD%\..
set CLASSPATH=./conf
set CLASSPATH=%CLASSPATH%;./lib/*


set JAVA_OPTS= -Xms32M -Xmx256M

set JAVA_OPTS=%JAVA_OPTS% -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false -Dfile.encoding=UTF-8

set JAVA_OPTS=%JAVA_OPTS% -Dagent-config-path=./conf/agent_example.config

set JAVA_OPTS=%JAVA_OPTS% -Dbroker-global-config-path=./conf/broker_global.config

 


rem ********* run broker ***********

@echo on

java %JAVA_OPTS% -cp "%CLASSPATH%" pt.com.broker.Start

pause
