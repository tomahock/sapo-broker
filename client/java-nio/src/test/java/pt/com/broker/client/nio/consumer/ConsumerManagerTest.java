package pt.com.broker.client.nio.consumer;


import junit.framework.Assert;
import pt.com.broker.client.nio.BaseTest;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.types.*;

/**
 * Created by luissantos on 08-05-2014.
 */
public class ConsumerManagerTest extends BaseTest {



    public void testNetPoolAddedAsQueue(){

        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification notification, HostInfo host) {
                return true;
            }

        };

        HostInfo host = new HostInfo("127.0.0.1",3323);

        consumerManager.addSubscription(netPoll, brokerListener,host);

        BrokerAsyncConsumer consumer = consumerManager.getConsumer(NetAction.DestinationType.QUEUE, destination, host);

        Assert.assertNotNull(consumer);

        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(NetAction.DestinationType.TOPIC, destination, host);

        Assert.assertNull(consumer2);


    }


    public void testNetSubribeQueue(){

        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetAction.DestinationType destinationType = NetAction.DestinationType.QUEUE;

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification notification, HostInfo host) {
                return true;
            }
        };

        HostInfo host = new HostInfo("127.0.0.1",3323);


        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        consumerManager.addSubscription(netSubscribeAction, brokerListener,host);


        BrokerAsyncConsumer consumer = consumerManager.getConsumer(destinationType, destination, host);

        Assert.assertNotNull(consumer);


        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(NetAction.DestinationType.TOPIC,destination, host);


        Assert.assertNull(consumer2);

    }



    public void testSubscriptionInvalidDestinationType(){


        ConsumerManager consumerManager = new ConsumerManager();

        String destination = "/teste/";

        NetAction.DestinationType destinationType =  null;

        NetPoll netPoll = new NetPoll(destination, 1000);


        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification notification, HostInfo host) {
                return true;
            }
        };

        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        try {

            consumerManager.addSubscription(netSubscribeAction, brokerListener, host);

            fail("Expecting Exception");
        }catch (IllegalArgumentException e){

        }

    }

    public void testSubscriptionInvalidDestination(){


        ConsumerManager consumerManager = new ConsumerManager();

        String destination = null;

        NetAction.DestinationType destinationType =  NetAction.DestinationType.TOPIC;

        NetPoll netPoll = new NetPoll(destination, 1000);

        HostInfo host = new HostInfo("127.0.0.1",3323);

        BrokerListener brokerListener = new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification message, HostInfo host) {
                return true;
            }
        };

        NetSubscribeAction netSubscribeAction = new NetSubscribe(destination, destinationType);

        try {

            consumerManager.addSubscription(netSubscribeAction, brokerListener, host);

            fail("Expecting Exception");
        }catch (IllegalArgumentException e){

        }

    }






}
