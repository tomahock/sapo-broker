package pt.com.broker.ws.models;

import pt.com.gcs.messaging.QueueProcessor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 25-06-2014.
 */
@JsonIgnoreProperties({"processor"})
public class Queue {
	
	public static final String JSON_PROP_NAME 						= "queue_name";
	public static final String JSON_PROP_MESSAGE_COUNT 				= "message_count";
	public static final String JSON_PROP_LAST_MESSAGE_DELIVERED 	= "message_last_delivery";

    private static DateFormat ISO_8601_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    static {
    	//FIXME: Fix this line...
        ISO_8601_DATE_TIME.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    private String name;
    private Long messageCount;
    private String lastMessageDelivered;
    
    public Queue(){
    	
    }

    @JsonCreator()
    public Queue(@JsonProperty(JSON_PROP_NAME) String name, 
    		@JsonProperty(JSON_PROP_MESSAGE_COUNT) long messageCount, 
    		@JsonProperty(JSON_PROP_LAST_MESSAGE_DELIVERED) String lastMessageDelivered) {
    	this.name = name;
    	this.messageCount = messageCount;
    	this.lastMessageDelivered = lastMessageDelivered;
    }
    
    @JsonProperty(JSON_PROP_NAME)
    public String getName(){
//        return processor.getSubscriptionName();
    	return name;
    }

    @JsonProperty(JSON_PROP_MESSAGE_COUNT)
    public Long getMessageCount(){
//        return processor.getQueuedMessagesCount();
    	return messageCount;
    }

    @JsonProperty(JSON_PROP_LAST_MESSAGE_DELIVERED)
    public String getLastMessageDelivered(){
    	return lastMessageDelivered;

//        long last = processor.lastMessageDelivered();
//
//        Date date = new Date();
//
//        date.setTime(last);
//
//        String outDate = ISO_8601_DATE_TIME.format(date);
//
//        return outDate;
    }

    @JsonProperty(JSON_PROP_NAME)
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty(JSON_PROP_MESSAGE_COUNT)
	public void setMessageCount(long messageCount) {
		this.messageCount = messageCount;
	}

	@JsonProperty(JSON_PROP_LAST_MESSAGE_DELIVERED)
	public void setLastMessageDelivered(String lastMessageDelivered) {
		this.lastMessageDelivered = lastMessageDelivered;
	}
}
