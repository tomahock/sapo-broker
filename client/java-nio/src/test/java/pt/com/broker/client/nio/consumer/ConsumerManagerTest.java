package pt.com.broker.client.nio.consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.consumer.BrokerAsyncConsumer;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.types.*;

/**
 * Created by luissantos on 08-05-2014.
 */
public class ConsumerManagerTest {


    @Test()
    public void testNetPoolAddedAsQueue(){

        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification notification) {
                return true;
            }

        };

        HostInfo host = new HostInfo("127.0.0.1",3323);

        consumerManager.addSubscription(netPoll, brokerListener,host);

        BrokerAsyncConsumer consumer = consumerManager.getConsumer(NetAction.DestinationType.QUEUE, destination, host.getSocketAddress());

        Assert.assertNotNull(consumer);

        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(NetAction.DestinationType.TOPIC, destination, host.getSocketAddress());

        Assert.assertNull(consumer2);


    }

    @Test()
    public void testNetSubribeQueue(){

        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetAction.DestinationType destinationType = NetAction.DestinationType.QUEUE;

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification notification) {
                return true;
            }
        };

        HostInfo host = new HostInfo("127.0.0.1",3323);


        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        consumerManager.addSubscription(netSubscribeAction, brokerListener,host);


        BrokerAsyncConsumer consumer = consumerManager.getConsumer(destinationType, destination, host.getSocketAddress());

        Assert.assertNotNull(consumer);


        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(NetAction.DestinationType.TOPIC,destination, host.getSocketAddress());


        Assert.assertNull(consumer2);

    }


    @Test(expected = IllegalArgumentException.class)
    public void testSubscriptionInvalidDestinationType(){


        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetAction.DestinationType destinationType =  null;

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification notification) {
                return true;
            }
        };

        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        consumerManager.addSubscription(netSubscribeAction, brokerListener, host);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubscriptionInvalidDestination(){


        ConsumerManager consumerManager = new ConsumerManager();

        String destination = null;

        NetAction.DestinationType destinationType =  NetAction.DestinationType.TOPIC;

        NetPoll netPoll = new NetPoll(destination, 1000);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification message) {
                return true;
            }
        };

        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        consumerManager.addSubscription(netSubscribeAction, brokerListener,host);

    }






}
