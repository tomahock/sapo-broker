package pt.com.broker.ws.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pt.com.broker.types.MessageListener;
import pt.com.gcs.messaging.SubscriptionProcessor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright (c) 2014, SAPO
 * All rights reserved.
 *

 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 24-06-2014.
 */
public class RemoteSubscription extends Subscription {

    public RemoteSubscription(SubscriptionProcessor topicProcessor) {
        super(topicProcessor);
    }

    @JsonProperty("listeners")
    public List<Listener> getListeners() {


        Collection<MessageListener> remoteListeners = topicProcessor.remoteListeners();

        List<Listener> listeners = new ArrayList<>(remoteListeners.size());

        for(MessageListener l : remoteListeners){
                listeners.add(new Listener(l));
        }

        return listeners;
    }
}
