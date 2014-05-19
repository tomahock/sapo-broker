# Sapo Broker Java nio client


## Connecting synchronously
```java

        BrokerClient bk = new BrokerClient();
       
        bk.addServer("broker.wallet.pt",3323);
        
        final ListenableFuture<HostInfo> f = bk.connect();
        
        HostInfo host = f.get(); //blocks
        
        if( host == null){
            //not connected
        }else{
            //connected
        }
        
```


## Connecting asynchronously
```java

        BrokerClient bk = new BrokerClient();
       
        bk.addServer("broker.wallet.pt",3323);
        
        final ListenableFuture<HostInfo> f = bk.connect();
        
        
        f.addListener(new Runnable() {
        
           @Override
           public void run() {
        
               try {
        
                   HostInfo host = f.get();
        
                   if(host == null){
                       log.debug("Not Connected");
                   }else{
                       log.debug("Connected");
                   }
        
        
               } catch (Throwable t) {
                   
                   t.printStackTrace();
               }
        
           }
        }, MoreExecutors.sameThreadExecutor());

        
```


##  subscribe a queue or topic

```java

        BrokerClient bk = new BrokerClient();
       
        // ... connecting ...
        
       bk.subscribe("/teste/",NetAction.DestinationType.QUEUE,new BrokerListenerAdapter() {
       
                   @Override
                   public boolean onMessage(NetMessage message) {
       
                       // do something
                       
                       return true; // return true or false to acknowledge or not 
                   }
       
       });

```

## publish a message

```java

        BrokerClient bk = new BrokerClient();
       
        // ... connecting ...
        
       NetAction.DestinationType dstType = NetAction.DestinationType.QUEUE; // or TOPIC 

       Future future = bk.publishMessage("Olá Mundo", "/teste/", dstType);
       
```

## Publishing messages via UDP
```java

       UdpBrokerClient bk = new UdpBrokerClient();
       
       bk.addServer("broker.wallet.pt",3323); 
       
       bk.connect().get(); // There is no connection when publishing over UDP but we still need this for compatibility  
        
       NetAction.DestinationType dstType = NetAction.DestinationType.QUEUE; // or TOPIC 

       Future future = bk.publishMessage("Olá Mundo", "/teste/", dstType);
```


## SSL Support
```java

       SslBrokerClient bk = new SslBrokerClient();
       
       bk.addServer("broker.wallet.pt",3390); // 3390 broker SSL port
       
       // by default it uses the jvm certificate authorities but you can change it
       bk.setContext( ... );
       
       // ... connecting ... 
       

```
