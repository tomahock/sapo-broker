package pt.com.broker.ws.models;

import pt.com.broker.types.MessageListener;
import pt.com.gcs.messaging.SubscriptionProcessor;
import pt.com.gcs.messaging.TopicProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public class LocalSubscription extends Subscription {

    public LocalSubscription(SubscriptionProcessor topicProcessor) {
//        super(topicProcessor);
    }

    @JsonProperty("listeners")
    public List<Listener> getListeners() {
    	return null;
//        Collection<MessageListener> locallisteners = topicProcessor.localListeners();
//
//        List<Listener> listeners = new ArrayList<>(locallisteners.size());
//
//        for(MessageListener l : locallisteners){
//                listeners.add(new Listener(l));
//        }
//
//        return listeners;
    }
}
