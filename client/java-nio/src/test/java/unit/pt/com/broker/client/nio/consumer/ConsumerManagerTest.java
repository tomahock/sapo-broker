package unit.pt.com.broker.client.nio.consumer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import pt.com.broker.client.nio.consumer.BrokerAsyncConsumer;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.BrokerListenerAdapter;
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


        BrokerListener brokerListener = new BrokerListenerAdapter() {
            @Override
            public void onMessage(NetMessage message) {

            }

            @Override
            public void onFault(NetMessage message) {

            }
        };

        consumerManager.addSubscription(netPoll, brokerListener);

        BrokerAsyncConsumer consumer = consumerManager.getConsumer(NetAction.DestinationType.QUEUE, destination);

        Assert.assertNotNull(consumer);

        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(NetAction.DestinationType.TOPIC, destination);

        Assert.assertNull(consumer2);


    }

    @Test()
    public void testNetSubribeQueue(){

        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetAction.DestinationType destinationType = NetAction.DestinationType.QUEUE;

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new BrokerListenerAdapter() {
            @Override
            public void onMessage(NetMessage message) {

            }

            @Override
            public void onFault(NetMessage message) {

            }
        };

        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        consumerManager.addSubscription(netSubscribeAction, brokerListener);


        BrokerAsyncConsumer consumer = consumerManager.getConsumer(destinationType,destination);

        Assert.assertNotNull(consumer);


        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(NetAction.DestinationType.TOPIC,destination);


        Assert.assertNull(consumer2);

    }


    @Test(expected = IllegalArgumentException.class)
    public void testSubscriptionInvalidDestinationType(){


        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetAction.DestinationType destinationType =  null;

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new BrokerListenerAdapter() {
            @Override
            public void onMessage(NetMessage message) {

            }

            @Override
            public void onFault(NetMessage message) {

            }
        };

        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        consumerManager.addSubscription(netSubscribeAction, brokerListener);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubscriptionInvalidDestination(){


        ConsumerManager consumerManager = new ConsumerManager();

        String destination = null;

        NetAction.DestinationType destinationType =  NetAction.DestinationType.TOPIC;

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new BrokerListenerAdapter() {
            @Override
            public void onMessage(NetMessage message) {

            }

            @Override
            public void onFault(NetMessage message) {

            }
        };

        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        consumerManager.addSubscription(netSubscribeAction, brokerListener);

    }






}
