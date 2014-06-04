package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPing;
import pt.com.broker.types.NetPong;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luissantos on 30-04-2014.
 */
public class PongConsumerManager {

    private static final Logger log = LoggerFactory.getLogger(PongConsumerManager.class);

    volatile ConcurrentHashMap<String,BrokerListener> pongMessages  = new ConcurrentHashMap<String, BrokerListener>();


    public PongConsumerManager() {

    }

    public BrokerListener removeSubscription(String actionId){

        if(actionId==null){
            throw new RuntimeException("Invalid actionID null");
        }

        return pongMessages.remove(actionId);
    }

    public void addSubscription(String actionId, BrokerListener listener){

        if(actionId==null){
            throw new RuntimeException("Invalid actionID null");
        }

        pongMessages.put(actionId, listener);
    }

    public void deliverMessage(NetMessage netMessage, HostInfo host) throws Throwable {

        String actionid =  netMessage.getAction().getPongMessage().getActionId();

        BrokerListener listener = pongMessages.get(actionid);


        if(listener==null) {
            log.error("Invalid action id: "+actionid+". Pong handler not found");
            return;
        }

        log.debug("Delivering pong message:  "+actionid);

        listener.deliverMessage(netMessage,host);

    }




}
