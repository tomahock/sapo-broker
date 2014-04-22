package pt.com.broker.client.nio.consumer;

import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetSubscribe;

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

    public void addSubscription(BrokerAsyncConsumer consumer){

        NetSubscribe subscribe = consumer.getSubscription();
        BrokerListener listener = consumer.getListener();

        Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(subscribe.getDestinationType());


        String destination = subscribe.getDestination();

        if (subscriptions.containsKey(destination))
        {

            throw new IllegalStateException("A listener for that Destination already exists");
        }


        subscriptions.put(destination,consumer);

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


    public void deliverMessage(NetMessage netMessage) throws Throwable {

        DestinationType dtype = netMessage.getAction().getNotificationMessage().getDestinationType();
        String destination = netMessage.getAction().getNotificationMessage().getSubscription();



        BrokerAsyncConsumer consumer = getConsumer(dtype,destination);


        consumer.deliver(netMessage.getAction().getNotificationMessage());

    }
}
