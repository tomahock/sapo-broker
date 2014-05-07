package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.types.NetMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by luissantos on 07-05-2014.
 */
public class PollConsumerManager {


    protected final Map<String, BrokerListener> _syncSubscriptions = new HashMap<String, BrokerListener>();


    public void addSubscription(String queueName, BrokerListener listener){

        synchronized (_syncSubscriptions){

            if(_syncSubscriptions.containsKey(queueName)){
                throw new IllegalArgumentException("Queue " + queueName + " has already a poll runnig.");
            }

            _syncSubscriptions.put(queueName,listener);

        }
    }


    public BrokerListener getSubscription(String queueName){
        return _syncSubscriptions.get(queueName);
    }

    public void deliverMessage(NetMessage netMessage, Channel channel) throws Throwable {

        synchronized (_syncSubscriptions){

            String queueName =  netMessage.getAction().getNotificationMessage().getDestination();

            BrokerListener listener = getSubscription(queueName);

            listener.deliverMessage(netMessage , channel);

            removeSubscription(queueName);
        }
    }

    public void removeSubscription(String queueName){
        _syncSubscriptions.remove(queueName);
    }



}
