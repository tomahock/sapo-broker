package pt.com.broker.ws.models;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
import pt.com.broker.types.MessageListener;
import pt.com.gcs.messaging.SubscriptionProcessor;
import pt.com.gcs.messaging.TopicProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public abstract class Subscription {


    protected final SubscriptionProcessor topicProcessor;


    public Subscription(SubscriptionProcessor topicProcessor) {
        this.topicProcessor = topicProcessor;
    }

    @JsonProperty("name")
    public String getName() {
        return topicProcessor.getSubscriptionName();
    }



}
