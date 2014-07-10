package pt.com.broker.ws.rest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.types.NetMessage;
import pt.com.broker.ws.models.Queue;
import pt.com.broker.ws.models.Subscription;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 25-06-2014.
 */
@Path("queues")
public class Queues {

    private static final Logger log = LoggerFactory.getLogger(Queues.class);

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Queue> getOpenQueues() {

        return getMessageQueues();
    }


    @DELETE()
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean deleteQueue(@PathParam("name") String name, @DefaultValue("false") @QueryParam("force") boolean force){


        try{

            deleteMessageQueue(name,!force);

            return true;

        }catch (Exception e){

            log.error("Error deleting queue",e);

            return false;
        }

    }


    @GET()
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Queue getQueue(@PathParam("name") String name){

        Queue queue = getMessageQueue(name);

        if(queue == null) {
            throw new WebApplicationException(404);
        }


        return queue;

    }

    @GET()
    @Path("/{name}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NetMessage> getQueueMessages(@PathParam("name") String name){

        Queue queue = getMessageQueue(name);

        if(queue == null) {
            throw new WebApplicationException(404);
        }


        List<NetMessage> netMessages = queue.getProcessor().getMessages();


        return netMessages;

    }


    private void deleteMessageQueue(String name,boolean safe){

        /* @todo fix bug in deletequeue */
        if(safe){
            Gcs.deleteQueue(name);
        }else {
            Gcs.deleteQueue(name,false);
        }

    }


    private  List<Queue> getMessageQueues(){

        List<Queue> queues = new ArrayList<>();

        for(QueueProcessor qp : QueueProcessorList.values()){
            queues.add(new Queue(qp));
        }

        return queues;
    }

    private Queue getMessageQueue(String name){

        for(QueueProcessor qp : QueueProcessorList.values()){
            if(qp.getSubscriptionName().equals(name)){
                    return new Queue(qp);
            }
        }

        return null;
    }



}
