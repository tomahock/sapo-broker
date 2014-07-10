package pt.com.broker.ws.models;

import org.codehaus.jackson.annotate.JsonIgnore;
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
public class Queue {

    private static DateFormat ISO_8601_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    static {
        ISO_8601_DATE_TIME.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    QueueProcessor processor;

    public Queue(QueueProcessor processor) {
        this.processor = processor;


    }



    @JsonProperty("name")
    public String getName(){
        return processor.getSubscriptionName();
    }

    @JsonProperty("count")
    public long getMessageCount(){
        return processor.getQueuedMessagesCount();
    }

    @JsonProperty("last_deliver")
    public String getLastDeliver(){

        long last = processor.lastMessageDelivered();

        Date date = new Date();

        date.setTime(last);

        String outDate = ISO_8601_DATE_TIME.format(date);

        return outDate;
    }

    @JsonIgnore()
    public QueueProcessor getProcessor() {
        return processor;
    }
}
