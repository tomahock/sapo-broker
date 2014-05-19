# Sapo Broker Java nio client


## Connecting synchronously
```java

        BrokerClient bk = new BrokerClient();
       
        bk.addServer("localhost",3323);
        
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
       
        bk.addServer("localhost",3323);
        
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


##  subscribe a queue

```java

        BrokerClient bk = new BrokerClient();
       
        // ... connecting ...
        
        bk.subscribe("/teste/",NetAction.DestinationType.QUEUE,new BrokerListenerAdapter() {
        
                    @Override
                    public void onMessage(NetMessage message) {
                        // do something with the message
                    }
        
         });

```