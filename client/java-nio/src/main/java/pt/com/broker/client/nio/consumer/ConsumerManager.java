package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.types.*;
import pt.com.broker.types.NetAction.DestinationType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luissantos on 22-04-2014.
 */
public class ConsumerManager {

    protected final EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>> _consumerList = new EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>>(NetAction.DestinationType.class);


    public ConsumerManager() {

        _consumerList.put(DestinationType.TOPIC, new ConcurrentHashMap<String, BrokerAsyncConsumer>());
        _consumerList.put(DestinationType.QUEUE, new ConcurrentHashMap<String, BrokerAsyncConsumer>()); // VIRTAL_QUEUE BEHAVES THE SAME WAY AS QUEUE

    }


    public void addSyncSubscription(NetPoll netPoll, BrokerListener listener){
        addSubscription(new BrokerAsyncConsumer(netPoll.getDestination(), DestinationType.QUEUE, listener));
    }

    public void addSubscription(NetSubscribeAction subscribe, BrokerListener listener){
        addSubscription(new BrokerAsyncConsumer(subscribe.getDestination(), subscribe.getDestinationType() , listener));
    }

    public void addSubscription(BrokerAsyncConsumer consumer){

        synchronized (_consumerList){

                Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(consumer.getDestinationType());

                String destination = consumer.getDestinationName();

                if (subscriptions.containsKey(destination))
                {
                    throw new IllegalArgumentException("A listener for the destination "+destination+" already exists");
                }

                subscriptions.put(destination,consumer);

        }

    }

    public BrokerAsyncConsumer getConsumer(DestinationType dtype , String destination){

        Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(dtype);

        return subscriptions.get(destination);

    }

    public Map<String, BrokerAsyncConsumer> getSubscriptions(NetAction.DestinationType dtype){

        /* VirtualQueue is also queue so we must test this */
        DestinationType type = DestinationType.TOPIC.equals(dtype) ? DestinationType.TOPIC : DestinationType.QUEUE;

        Map<String, BrokerAsyncConsumer> subscriptions =  _consumerList.get(type);

        return subscriptions;

    }


    public void deliverMessage(NetMessage netMessage, Channel channel) throws Throwable {

        DestinationType dtype = null;
        String destination = null;

        if(netMessage.getAction().getNotificationMessage()!=null) {

            dtype = netMessage.getAction().getNotificationMessage().getDestinationType();
            destination = netMessage.getAction().getNotificationMessage().getSubscription();

            BrokerAsyncConsumer consumer = getConsumer(dtype,destination);

            consumer.deliver(netMessage, channel);
        }





    }
}
