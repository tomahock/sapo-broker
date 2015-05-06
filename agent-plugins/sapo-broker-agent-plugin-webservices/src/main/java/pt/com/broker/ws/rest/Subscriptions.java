package pt.com.broker.ws.rest;

import pt.com.broker.types.MessageListener;
import pt.com.broker.ws.models.Listener;
import pt.com.broker.ws.models.LocalSubscription;
import pt.com.broker.ws.models.RemoteSubscription;
import pt.com.broker.ws.models.Subscription;
import pt.com.gcs.messaging.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
@Path("subscriptions")
public class Subscriptions {

    @GET
    @Path("/{subscription:topic|queue}/{type:local|remote}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Subscription> getSubscription(@PathParam("subscription") String subscriptionType, @PathParam("type") String type ,@QueryParam("name") String name) {
        SubscriptionProcessorList processorList;
        if(subscriptionType.equals("queue")){
            processorList = QueueProcessorList.getInstance();
        }else{
            processorList = TopicProcessorList.getInstance();
        }
        List<Subscription> subscriptions;
        if(name == null || name.isEmpty()){
            subscriptions = getListeners(processorList, type.equals("local"));
        }else{
            Subscription subscription = getProcessor(processorList, name, type.equals("local"));
            subscriptions = new ArrayList<>();
            if(subscription!=null){
                subscriptions.add(subscription);
            }
        }
        return subscriptions;
    }




    private final Subscription getProcessor(SubscriptionProcessorList processorList , String name , boolean local){
//        SubscriptionProcessor topicProcessor = processorList.getSubscriptionProcessor(name);
//        if(topicProcessor==null){
//            return null;
//        }
//        if (local) {
//            if(topicProcessor.hasLocalListeners()){
//                return new LocalSubscription(topicProcessor);
//            }
//        }else{
//            if(topicProcessor.hasRemoteListeners()){
//                return new RemoteSubscription(topicProcessor);
//            }
//        }
        return null;
    }

    private final List<Subscription>  getListeners(SubscriptionProcessorList processorList, boolean local){
    	Map<String, Subscription> subs = new HashMap<>();
        for (SubscriptionProcessor tp : processorList.getValues())
        {
        	Subscription sub = subs.get(tp.getSubscriptionName());
        	if(sub == null){
        		sub = new Subscription(tp.getSubscriptionName());
        		subs.put(tp.getSubscriptionName(), sub);
        	}
        	Set<MessageListener> listeners = tp.localListeners();
        	if(!local){
        		listeners = tp.remoteListeners();
        	}
        	for(MessageListener listener: listeners){
    			InetSocketAddress addr = (InetSocketAddress) listener.getChannel().getChannel().remoteAddress();
    			Listener l = new Listener();
    			l.setHostName(addr.getHostName());
    			l.setPort(addr.getPort());
    			if(local){
    				sub.addLocalListener(l);
    			} else {
    				sub.addRemoteListener(l);
    			}
    		}
        }
        return new ArrayList<Subscription>(subs.values());
    }



}
