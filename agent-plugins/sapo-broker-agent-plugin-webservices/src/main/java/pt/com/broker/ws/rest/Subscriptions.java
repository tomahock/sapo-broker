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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public List<Subscription> getSubscription(@PathParam("subscription") String subscription_type, @PathParam("type") String type ,@QueryParam("name") String name) {

        SubscriptionProcessorList processorList;

        if(subscription_type.equals("queue")){
            processorList = QueueProcessorList.getInstance();
        }else{
            processorList = TopicProcessorList.getInstance();
        }




        List<Subscription> subscriptions;

        if(name == null || name.isEmpty()){
            subscriptions =   getListeners(processorList, type.equals("local"));
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

        SubscriptionProcessor topicProcessor = processorList.getSubscriptionProcessor(name);


        if(topicProcessor==null){
            return null;
        }


        if (local) {

            if(topicProcessor.hasLocalListeners()){
                return new LocalSubscription(topicProcessor);
            }
        }else{
            if(topicProcessor.hasRemoteListeners()){
                return new RemoteSubscription(topicProcessor);
            }
        }

        return null;

    }

    private final List<Subscription>  getListeners(SubscriptionProcessorList processorList, boolean local){

        List<Subscription> subscriptions = new ArrayList<>();



        for (SubscriptionProcessor tp : processorList.getValues())
        {


                if (local) {

                    if(tp.hasLocalListeners()) {
                        subscriptions.add(new LocalSubscription(tp));
                    }

                } else {

                    if(tp.hasRemoteListeners()) {

                        subscriptions.add(new RemoteSubscription(tp));

                    }

                }


        }

        return subscriptions;

    }



}
