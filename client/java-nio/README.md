# Sapo Broker Java nio client


```java

        BrokerClient bk = new BrokerClient();
        
        bk.addServer("localhost",3323);
        
        
        Future<HostInfo> f = bk.connect();
        
        f.addListener(new ChannelFutureListener() {
        
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
            
                System.out.println("Connected");
                
            }
            
        });
 
        
        
```