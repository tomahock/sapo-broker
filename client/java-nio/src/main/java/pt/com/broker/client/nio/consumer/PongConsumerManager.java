package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPing;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luissantos on 30-04-2014.
 */
public class PongConsumerManager {

    private static final Logger log = LoggerFactory.getLogger(PongConsumerManager.class);

    Map<String,BrokerListener> pongMessages  = new HashMap<String, BrokerListener>();


    public PongConsumerManager() {

    }


    public void addSubscription(NetPing ping, BrokerListener listener){

        pongMessages.put(ping.getActionId(),listener);

    }

    public void deliverMessage(NetMessage netMessage, Channel channel) throws Throwable {

        String actionid =  netMessage.getAction().getPongMessage().getActionId();

        BrokerListener listener = pongMessages.get(actionid);

        if(listener==null){
            log.error("Invalid action id: "+actionid+". Pong handler not found");
            return;
        }

        log.debug("Delivering pong message:  "+actionid);

        listener.deliverMessage(netMessage,channel);

    }


}
