---
layout: broker-documentation
title: Java NIO client - Advanced Acknowledge Request
site_root:  /
tags:
---

Sometimes you need explicit acknowledge from the server.


##  Subscribe a queue/topic with explicit acknowledge

```java

        BrokerClient bk = new BrokerClient();
       
        // ... connecting ...
        
       String actionID = UUID.randomUUID().toString();
       
       long acknowledgeTimeout = 2000; // 2 seconds
        
       AcceptRequest acceptRequest = new AcceptRequest(actionID, new  AcceptResponseListener(){
        
                    @Override
                    public void onMessage(NetAccepted message, HostInfo host) {
                        
                        /*
                            message was accepted and acknowledge
                            message.getActionId() must be equal to actionID
        
                         */
        
                    }
        
                    @Override
                    public void onFault(NetFault message, HostInfo host) {
                    
                        // message was accepted but there was something wrong
                        
                    }
        
                    @Override
                    public void onTimeout(String actionID) {
                        
                        // neither success or fault message were received 
                         
                    }
                    
       },acknowledgeTimeout);
        
       Future<HostInfo> f = bk.subscribe("/teste/",NetAction.DestinationType.QUEUE,new NotificationListenerAdapter() {
       
                   @Override
                   public boolean onMessage(NetNotification message, HostInfo host) {
       
                       // do something
                       
                       return true; // return true or false to acknowledge or not 
                   }
       
       },acceptRequest);
       
       
       f.get(); //wait for subscription message to be sent

```