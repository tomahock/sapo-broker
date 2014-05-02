package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.PongListenerAdapter;
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

    ConcurrentHashMap<String,BrokerListener> pongMessages  = new ConcurrentHashMap<String, BrokerListener>();


    public PongConsumerManager() {

    }


    public void addSubscription(NetPing ping, BrokerListener listener){

        String actionid = (ping.getActionId()==null) ? NetPong.getUniversalActionId() : ping.getActionId();

        pongMessages.put(actionid, listener);
    }

    public void deliverMessage(NetMessage netMessage, Channel channel) throws Throwable {

        String actionid =  netMessage.getAction().getPongMessage().getActionId();

        BrokerListener listener = pongMessages.get(actionid);




        if(listener==null) {
            log.error("Invalid action id: "+actionid+". Pong handler not found");
            return;
        }

        log.debug("Delivering pong message:  "+actionid);

        listener.deliverMessage(netMessage,channel);

    }




}
