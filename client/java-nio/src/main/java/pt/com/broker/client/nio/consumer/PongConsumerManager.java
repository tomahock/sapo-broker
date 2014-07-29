package pt.com.broker.client.nio.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.ActionIdDecorator;
import pt.com.broker.types.NetMessage;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luissantos on 30-04-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class PongConsumerManager {

    private static final Logger log = LoggerFactory.getLogger(PongConsumerManager.class);

    volatile ConcurrentHashMap<String,BrokerListener> pongMessages  = new ConcurrentHashMap<String, BrokerListener>();


    /**
     * <p>Constructor for PongConsumerManager.</p>
     */
    public PongConsumerManager() {

    }

    /**
     * <p>removeSubscription.</p>
     *
     * @param actionId a {@link java.lang.String} object.
     * @return a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     */
    public BrokerListener removeSubscription(String actionId){

        if(actionId==null){
            throw new RuntimeException("Invalid actionID null");
        }

        return pongMessages.remove(actionId);
    }

    /**
     * <p>addSubscription.</p>
     *
     * @param actionId a {@link java.lang.String} object.
     * @param listener a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     */
    public void addSubscription(String actionId, BrokerListener listener){

        if(actionId==null){
            throw new RuntimeException("Invalid actionID null");
        }

        pongMessages.put(actionId, listener);
    }

    /**
     * <p>deliverMessage.</p>
     *
     * @param netMessage a {@link pt.com.broker.types.NetMessage} object.
     * @param host a {@link pt.com.broker.client.nio.server.HostInfo} object.
     * @throws java.lang.Throwable if any.
     */
    public void deliverMessage(NetMessage netMessage, HostInfo host) throws Throwable {

        String actionid = getActionId(netMessage);

        BrokerListener listener = pongMessages.get(actionid);


        if(listener==null) {
            throw new IllegalArgumentException("No listener found for the actionId: "+actionid);
        }

        log.debug("Delivering pong message:  "+actionid);

        listener.deliverMessage(netMessage,host);

    }

    private String getActionId(NetMessage msg){

        ActionIdDecorator decorator = new ActionIdDecorator(msg);


        return decorator.getActionId();
    }



}
