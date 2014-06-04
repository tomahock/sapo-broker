# Sapo Broker Java nio client


## Connecting synchronously
```java

        BrokerClient bk = new BrokerClient();
       
        bk.addServer("broker.wallet.pt",3323);
        
        bk.connect(); // if connection is not possible a runtime exception will be thrown 
               
```


## Connecting asynchronously
```java

        BrokerClient bk = new BrokerClient();
       
        bk.addServer("broker.wallet.pt",3323);
        
        Future<HostInfo> f = bk.connectAsync();
        
       
        f.get(); if connection is not possible a runtime exception will be thrown 
        
```


##  subscribe a queue or topic

```java

        BrokerClient bk = new BrokerClient();
       
        // ... connecting ...
        
       bk.subscribe("/teste/",NetAction.DestinationType.QUEUE,new NotificationListenerAdapter() {
       
                   @Override
                   public boolean onMessage(NetNotification message, HostInfo host) {
       
                       // do something
                       
                       return true; // return true or false to acknowledge or not 
                   }
       
       });

```


##  subscribe a queue using polling 

```java

        BrokerClient bk = new BrokerClient();
             
        // ... connecting ...

        while (true){
      
                    
                  NetNotification notification = bk.poll("/teste/"); //blocks!!!
                  
                  // ... do some work ... 
                
                  bk.acknowledge(notification); //acknowledge the message
                     
      
                  if( ... ){ // step out on some condition
                        break;
                  }
      
        }

```


##  subscribe a queue using polling with timeout 

```java

        BrokerClient bk = new BrokerClient();
             
        // ... connecting ...

        long timeout = 5000;
        while (true){
      
                   try{
                        
                        NetNotification notification = bk.poll("/teste/",timeout); //blocks!!!
                        
                        // ... do some work ... 
                                        
                        bk.acknowledge(notification); //acknowledge the message
                        
                  
                   }catch (TimeoutException e){

                        // there was a timeout

                    }
                
                  
                    if( ... ){ // step out on some condition
                        break;
                    }
      
        }

```
  
## publish a message

```java

        BrokerClient bk = new BrokerClient();
       
        // ... connecting ...
        
       NetAction.DestinationType dstType = NetAction.DestinationType.QUEUE; // or TOPIC 

       Future future = bk.publish("Olá Mundo", "/teste/", dstType);
       
```

## Publishing messages via UDP
```java

       UdpBrokerClient bk = new UdpBrokerClient();
       
       bk.addServer("broker.wallet.pt",3323); 
       
       bk.connect().get(); // There is no connection when publishing over UDP but we still need this for compatibility  
        
       NetAction.DestinationType dstType = NetAction.DestinationType.QUEUE; // or TOPIC 

       Future future = bk.publish("Olá Mundo", "/teste/", dstType);
```


## SSL Support
```java

       SslBrokerClient bk = new SslBrokerClient();
       
       bk.addServer("broker.wallet.pt",3390); // 3390 broker SSL port
       
       // by default it uses the jvm certificate authorities but you can change it
       bk.setContext( ... );
       
       // ... connecting ... 
       
```

# Advanced Topics

* [Advanced Message Handling](doc/advanced/message-handling.md)
* [SSL Advanced Usage](doc/advanced/ssl.md)
