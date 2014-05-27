package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.client.nio.types.DestinationDataFactory;
import pt.com.broker.types.*;
import pt.com.broker.types.NetAction.DestinationType;

import java.net.InetSocketAddress;
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

    private static final Logger log = LoggerFactory.getLogger(ConsumerManager.class);

    protected final EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>> _consumerList = new EnumMap<NetAction.DestinationType, Map<String, BrokerAsyncConsumer>>(NetAction.DestinationType.class);



    public ConsumerManager() {

        _consumerList.put(DestinationType.TOPIC, new ConcurrentHashMap<String, BrokerAsyncConsumer>());
        _consumerList.put(DestinationType.QUEUE, new ConcurrentHashMap<String, BrokerAsyncConsumer>()); // VIRTAL_QUEUE BEHAVES THE SAME WAY AS QUEUE

    }




    public void addSubscription(NetSubscribeAction subscribe, BrokerListener listener, HostInfo hostInfo){

        BrokerAsyncConsumer consumer = new BrokerAsyncConsumer(subscribe.getDestination(), subscribe.getDestinationType() , listener);

        consumer.setHost(hostInfo);

        addSubscription(consumer);
    }

    private String getDestinationKey(String destination , InetSocketAddress socketAddress){

        if(StringUtils.isEmpty(destination)){
            throw new IllegalArgumentException("Invalid Destination");
        }
        String hostname = socketAddress.getHostName();
        int port = socketAddress.getPort();
        return hostname + ":" + port +"#"+ destination;
    }

    public void addSubscription(BrokerAsyncConsumer consumer){

        DestinationType destinationType = consumer.getDestinationType();


        if(destinationType == null){
            throw new IllegalArgumentException("Invalid Destination Type");
        }

        String destination = getDestinationKey(consumer.getDestinationName(),consumer.getHost().getSocketAddress());

        synchronized (_consumerList){

                Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);


                if (subscriptions.containsKey(destination))
                {
                    throw new IllegalArgumentException("A listener for the destination "+destination+" already exists");
                }

                subscriptions.put(destination,consumer);
                log.info("Added Async Consumer for {} {} ", consumer.getHost().getSocketAddress(), consumer.getDestinationName());
        }

    }

    public BrokerAsyncConsumer removeSubscription(NetSubscribeAction netSubscribeAction, InetSocketAddress socketAddress){
        return removeSubscription(netSubscribeAction.getDestinationType(),netSubscribeAction.getDestination(), socketAddress);
    }

    public BrokerAsyncConsumer removeSubscription(DestinationType destinationType, String destination , InetSocketAddress socketAddress ){

        synchronized (_consumerList) {
            Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);

            String key = getDestinationKey(destination, socketAddress);

            BrokerAsyncConsumer brokerAsyncConsumer =  subscriptions.remove(key);

            if(brokerAsyncConsumer!=null){
                log.debug("Removing key: "+key);
            }

            return brokerAsyncConsumer;
        }
    }


    public BrokerAsyncConsumer getConsumer(DestinationType destinationType , String destination ,  InetSocketAddress socketAddress){

        Map<String, BrokerAsyncConsumer> subscriptions = getSubscriptions(destinationType);

        return subscriptions.get(getDestinationKey(destination,socketAddress));

    }

    protected BrokerAsyncConsumer getConsumer(NetMessage netMessage, InetSocketAddress socketAddress){

        DestinationDataFactory factory = new DestinationDataFactory();

        String destination = factory.getSubscription(netMessage);
        DestinationType dtype = factory.getDestinationType(netMessage);

        return getConsumer(dtype,destination,socketAddress);

    }

    public Map<String, BrokerAsyncConsumer> getSubscriptions(NetAction.DestinationType dtype){

        /* VirtualQueue is also queue so we must test this */
        DestinationType type = DestinationType.TOPIC.equals(dtype) ? DestinationType.TOPIC : DestinationType.QUEUE;

        Map<String, BrokerAsyncConsumer> subscriptions =  _consumerList.get(type);

        return subscriptions;

    }


    public void deliverMessage(NetMessage netMessage, Channel channel) throws Throwable {

        BrokerAsyncConsumer consumer = getConsumer(netMessage, (InetSocketAddress) channel.remoteAddress());


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

        synchronized (_consumerList) {

            Map<String, BrokerAsyncConsumer> map = new HashMap<>(2);

            for (Map.Entry<String, BrokerAsyncConsumer> entry : getSubscriptions(dtype).entrySet()) {

                String key = entry.getKey();
                BrokerAsyncConsumer consumer = entry.getValue();

                if(removeSubscription(dtype, consumer.getDestinationName(), host.getSocketAddress())!=null){
                    map.put(key, consumer);
                }

            }

            return map;
        }
    }





}
