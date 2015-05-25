# Sapo Broker Java nio client


# Building

```bash

 cd client/java-nio/
 mvn clean package               
```

 copy jar:  target/sapo-broker-java-client-nio-4.0.50.Alpha4.jar    
 copy dependency: target/dependency/*.jar
 
# Building a single jar with all dependencies
 
```bash

 cd client/java-nio/
 mvn clean compile assembly:single           
```