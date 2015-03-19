package pt.com.broker.ws.models;

import pt.com.gcs.messaging.SubscriptionProcessor;

import com.fasterxml.jackson.annotation.JsonProperty;

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
