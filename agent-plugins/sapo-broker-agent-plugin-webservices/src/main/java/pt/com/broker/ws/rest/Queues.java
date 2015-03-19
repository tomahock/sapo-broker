package pt.com.broker.ws.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.ws.models.Message;
import pt.com.broker.ws.models.Queue;
import pt.com.broker.ws.responses.MessageList;
import pt.com.broker.ws.responses.QueueList;
import pt.com.broker.ws.responses.QueueMessages;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 25-06-2014.
 */
@Path("/queues")
@Api(value="/queues" , description = "Operations on queues maintained by the agent.")
@Produces({"application/json"})
public class Queues {

    private static final Logger log = LoggerFactory.getLogger(Queues.class);

    @GET()
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
    		value = "Get all queues.",
    		notes = "Gets all existing message queues mainained by the agent.",
    		response = QueueList.class
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "Returns all agent queues.", response = QueueList.class)
    })
    public QueueList getOpenQueues() {
        return new QueueList(getMessageQueues());
    }

    //FIXME: Add Generic response method
    @DELETE()
    @Path("/{name : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
    		value = "Delete existing queue.",
    		notes = "Deletes the message queue with the name provided by the name parameter." +
    		"The force parameter is used to indicate if the queue should be eliminated even if " +
    		"it haves consumers.",
    		response = Boolean.class
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "The agent status information.", response = Boolean.class),
    		@ApiResponse(code = 404, message = "The message queue was not found.", response = Boolean.class)
    })
    public boolean deleteQueue(@PathParam("name") String name, @DefaultValue("false") @QueryParam("force") boolean force){
    	Queue q = getMessageQueue(name);
    	if(q == null) {
            throw new WebApplicationException(404);
        }
        try{
            deleteMessageQueue(name,!force);
            return true;
        }catch (Exception e){
            log.error("Error deleting queue",e);
            return false;
        }
    }


    @GET()
    @Path("/{name : .+}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
    		value = "Get message queue.",
    		notes = "Returns the message queue with the name provided by the name parameter.",
    		response = Queue.class
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "The queue with the provided name.", response = Queue.class),
    		@ApiResponse(code = 404, message = "The message queue was not found.")
    })
    public Queue getQueue(@PathParam("name") String name){
        Queue queue = getMessageQueue(name);
        if(queue == null) {
            throw new WebApplicationException(404);
        }
        return queue;
    }

    @GET()
    @Path("/{name : .+}/messages")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
    		value = "Get queue messages.",
    		notes = "Returns the messages contained in the message queue.",
    		response = QueueMessages.class
    )
    @ApiResponses(value = {
    		@ApiResponse(code = 200, message = "The messages contained by the queue with the provided name.", response = Queue.class),
    		@ApiResponse(code = 404, message = "The message queue was not found.")
    })
    public QueueMessages getQueueMessages(@PathParam("name") String name){
        Queue queue = getMessageQueue(name);
        if(queue == null) {
            throw new WebApplicationException(404);
        }
        QueueProcessor processor = QueueProcessorList.get(name);
        List<NetMessage> netMessages = processor.getMessages();
        List<Message> messages = new ArrayList<Message>();
        for(NetMessage nm: netMessages){
        	if(nm.getAction().getActionType() == ActionType.NOTIFICATION){
        		//It's the only message that should be in the queue
        		Message m = new Message(
        				nm.getAction().getNotificationMessage().getMessage().getMessageId(),
        				nm.getHeaders(),
        				nm.getAction().getActionType().name(),
        				nm.getAction().getNotificationMessage().getDestination(),
        				nm.getAction().getNotificationMessage().getDestinationType()
        		);
        		messages.add(m);
        	} else {
        		log.warn("The queue {} contains a message of type {}.", name, nm.getAction().getActionType().name());
        	}
        	
        }
        QueueMessages queueMessages = new QueueMessages(processor.getSubscriptionName(), new MessageList(messages));
        return queueMessages;
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
        	Queue q = new Queue(
        		qp.getSubscriptionName(), qp.getQueuedMessagesCount(), new Date(qp.lastMessageDelivered()).toString()
        	);
            queues.add(q);
        }
        return queues;
    }

    private Queue getMessageQueue(String name){
        for(QueueProcessor qp : QueueProcessorList.values()){
            if(qp.getSubscriptionName().equals(name)){
            	Queue q = new Queue(
            		qp.getSubscriptionName(), qp.getQueuedMessagesCount(), new Date(qp.lastMessageDelivered()).toString()
            	);
                return q;
            }
        }
        return null;
    }
    
}