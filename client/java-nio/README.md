# Sapo Broker Java nio client


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