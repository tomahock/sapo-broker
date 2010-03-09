#!/bin/bash

java -cp ./lib/h2*.jar org.h2.tools.Server -webPort 8899 -baseDir ~/Work/ActiveProjects/sapo/BrokerV4/monitorization/db

#jdbc:h2:~/Work/ActiveProjects/sapo/BrokerV3/BrokerPublicRepo/monitorization/db/brokerinfo
