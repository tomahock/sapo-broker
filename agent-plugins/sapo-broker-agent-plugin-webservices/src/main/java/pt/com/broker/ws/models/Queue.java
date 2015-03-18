package pt.com.broker.ws.models;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import pt.com.gcs.messaging.QueueProcessor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 25-06-2014.
 */
@JsonIgnoreProperties({"processor"})
public class Queue {

	//FIXME: This should be a placeholder only. The getter fields are being calculated and they should
	//not.
	
    private static DateFormat ISO_8601_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    static {
    	//FIXME: Fix this line...
        ISO_8601_DATE_TIME.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    private final String name;
    private final long messageCount;
    private final String lastMessageDelivered;

    public Queue(String name, long messageCount, String lastMessageDelivered) {
    	this.name = name;
    	this.messageCount = messageCount;
    	this.lastMessageDelivered = lastMessageDelivered;
    }
    
    @JsonProperty("name")
    public String getName(){
//        return processor.getSubscriptionName();
    	return name;
    }

    @JsonProperty("count")
    public long getMessageCount(){
//        return processor.getQueuedMessagesCount();
    	return messageCount;
    }

    @JsonProperty("last_deliver")
    public String getLastDeliver(){
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
}
