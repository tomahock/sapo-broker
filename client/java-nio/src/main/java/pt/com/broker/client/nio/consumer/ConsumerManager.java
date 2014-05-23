package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.caudexorigo.text.StringUtils;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.client.nio.types.DestinationDataFactory;
import pt.com.broker.types.*;
import pt.com.broker.types.NetAction.DestinationType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by luissantos on 22-04-2014.
 */
public class ConsumerManager {

    protected final EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>> _consumerList = new EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>>(NetAction.DestinationType.class);



    public ConsumerManager() {

        _consumerList.put(DestinationType.TOPIC, new ConcurrentHashMap<String, BrokerAsyncConsumer>());
        _consumerList.put(DestinationType.QUEUE, new ConcurrentHashMap<String, BrokerAsyncConsumer>()); // VIRTAL_QUEUE BEHAVES THE SAME WAY AS QUEUE

    }


    public void addSubscription(NetSubscribeAction subscribe, BrokerListener listener){
        this.addSubscription(subscribe,listener,null);
    }

    public void addSubscription(NetSubscribeAction subscribe, BrokerListener listener, HostInfo hostInfo){

        BrokerAsyncConsumer consumer = new BrokerAsyncConsumer(subscribe.getDestination(), subscribe.getDestinationType() , listener);

        consumer.setHost(hostInfo);

        addSubscription(consumer);
    }

    public void addSubscription(BrokerAsyncConsumer consumer){

        DestinationType destinationType = consumer.getDestinationType();

        if(destinationType==null){
            throw new IllegalArgumentException("Invalid Destination Type");
        }

        String destination = consumer.getDestinationName();
        if(StringUtils.isEmpty(destination)){
            throw new IllegalArgumentException("Invalid Destination");
        }

        synchronized (_consumerList){

                Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);


                if (subscriptions.containsKey(destination))
                {
                    throw new IllegalArgumentException("A listener for the destination "+destination+" already exists");
                }

                subscriptions.put(destination,consumer);

        }

    }

    public BrokerAsyncConsumer removeSubscription(NetSubscribeAction netSubscribeAction){
        return removeSubscription(netSubscribeAction.getDestinationType(),netSubscribeAction.getDestination());
    }

    public BrokerAsyncConsumer removeSubscription(DestinationType destinationType, String destination){

        Map<String, BrokerAsyncConsumer> subscriptions  = getSubscriptions(destinationType);


        return subscriptions.remove(destination);
    }


    public BrokerAsyncConsumer getConsumer(DestinationType destinationType , String destination){

        Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);

        return subscriptions.get(destination);

    }

    protected BrokerAsyncConsumer getConsumer(NetMessage netMessage){

        DestinationDataFactory factory = new DestinationDataFactory();

        String destination = factory.getSubscription(netMessage);
        DestinationType dtype = factory.getDestinationType(netMessage);

        return getConsumer(dtype,destination);

    }

    public Map<String, BrokerAsyncConsumer> getSubscriptions(NetAction.DestinationType dtype){

        /* VirtualQueue is also queue so we must test this */
        DestinationType type = DestinationType.TOPIC.equals(dtype) ? DestinationType.TOPIC : DestinationType.QUEUE;

        Map<String, BrokerAsyncConsumer> subscriptions =  _consumerList.get(type);

        return subscriptions;

    }


    public void deliverMessage(NetMessage netMessage, Channel channel) throws Throwable {

        BrokerAsyncConsumer consumer = getConsumer(netMessage);


        if(consumer == null){
            throw new RuntimeException("No consumer found for this message");
        }


        consumer.deliver(netMessage, channel);

    }


    public Map<String, BrokerAsyncConsumer> getSubscriptions(NetAction.DestinationType dtype, HostInfo host){

        Map<String, BrokerAsyncConsumer> map = new HashMap<String, BrokerAsyncConsumer>();

        for(Map.Entry<String, BrokerAsyncConsumer> entry  : getSubscriptions(dtype).entrySet()){
            String key = entry.getKey();
            BrokerAsyncConsumer consumer = entry.getValue();

            if(consumer.getHost().equals(host)){
                map.put(key,consumer);
            }
        }

        return map;
    }

    public Map<String, BrokerAsyncConsumer> removeSubscriptions(NetAction.DestinationType dtype, HostInfo host){

        Map<String, BrokerAsyncConsumer> map =  getSubscriptions(dtype,host);

        for(Map.Entry<String, BrokerAsyncConsumer> entry  : getSubscriptions(dtype).entrySet()){
            String key = entry.getKey();
            BrokerAsyncConsumer consumer = entry.getValue();

            removeSubscription(dtype,consumer.getDestinationName());

            map.put(key,consumer);

        }

        return map;
    }


}
