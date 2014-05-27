package pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.consumer.BrokerAsyncConsumer;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by luissantos on 08-05-2014.
 */
@RunWith(Parameterized.class)
public class SubscriberManagerTest {

    protected NetAction.DestinationType destinationType = null;
    protected  String destinationName = null;


    public SubscriberManagerTest(NetAction.DestinationType destinationType, String destinationName) {
        this.destinationType = destinationType;
        this.destinationName = destinationName;
    }

    @Parameterized.Parameters
    public static Collection primeNumbers() {
        return Arrays.asList(new Object[][]{
                {NetAction.DestinationType.QUEUE,"/teste/"},
                {NetAction.DestinationType.TOPIC,"/teste/"},
        });
    }


    @Test
    public void testSubscription(){

        ConsumerManager consumerManager = new ConsumerManager();

        BrokerAsyncConsumer consumer = new BrokerAsyncConsumer(destinationName, destinationType, new BrokerListener() {
            @Override
            public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

            }

            @Override
            public void setBrokerClient(BrokerClient client) {

            }
        });

        consumer.setHost(new HostInfo("127.0.0.1",3323));

        consumerManager.addSubscription(consumer);

    }

    @Test(expected = IllegalArgumentException.class )
    public void testSubscriptionDuplicatedQueue(){

        ConsumerManager consumerManager = new ConsumerManager();

        BrokerAsyncConsumer consumer = new BrokerAsyncConsumer(destinationName, destinationType, new BrokerListener() {
            @Override
            public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

            }

            @Override
            public void setBrokerClient(BrokerClient client) {

            }
        });

        consumer.setHost(new HostInfo("127.0.0.1",3323));

        consumerManager.addSubscription(consumer);


        try{

            BrokerAsyncConsumer consumer2 = new BrokerAsyncConsumer(destinationName, destinationType, new BrokerListener() {
                @Override
                public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

                }

                @Override
                public void setBrokerClient(BrokerClient client) {

                }
            });
            consumer2.setHost(new HostInfo("127.0.0.1",3323));

            consumerManager.addSubscription(consumer2);

        }catch (IllegalArgumentException ex){

            System.out.println(ex.getMessage());
            Assert.assertEquals("Invalid message", "A listener for the destination localhost:3323#/teste/ already exists",ex.getMessage());

            throw ex;
        }


    }


    @Test()
    public void testSubscriptionRemove(){

        ConsumerManager consumerManager = new ConsumerManager();



        BrokerAsyncConsumer consumer = new BrokerAsyncConsumer(destinationName, destinationType, new BrokerListener() {

            @Override
            public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

            }

            @Override
            public void setBrokerClient(BrokerClient client) {

            }
        });


        HostInfo host = new HostInfo("123.0.0.1",3323);
        consumer.setHost(host);

        consumerManager.addSubscription(consumer);


        BrokerAsyncConsumer asyncConsumer = consumerManager.removeSubscription(destinationType,destinationName,host.getSocketAddress());

        Assert.assertNotNull(asyncConsumer);

        Assert.assertSame(consumer,asyncConsumer);


        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(destinationType,destinationName,host.getSocketAddress());

        Assert.assertNull(consumer2);

    }

}
