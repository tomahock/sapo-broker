package unit.pt.com.broker.client.nio.consumer;

import io.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pt.com.broker.client.nio.BrokerClient;
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

        consumerManager.addSubscription(new BrokerAsyncConsumer(destinationName, destinationType, new BrokerListener() {
            @Override
            public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

            }

            @Override
            public void setBrokerClient(BrokerClient client) {

            }
        }));

    }

    @Test(expected = IllegalArgumentException.class )
    public void testSubscriptionDuplicatedQueue(){

        ConsumerManager consumerManager = new ConsumerManager();

        consumerManager.addSubscription(new BrokerAsyncConsumer(destinationName, destinationType, new BrokerListener() {
            @Override
            public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

            }

            @Override
            public void setBrokerClient(BrokerClient client) {

            }
        }));


        try{


        consumerManager.addSubscription(new BrokerAsyncConsumer(destinationName, destinationType, new BrokerListener() {
            @Override
            public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

            }

            @Override
            public void setBrokerClient(BrokerClient client) {

            }
        }));

        }catch (IllegalArgumentException ex){

            Assert.assertEquals("Invalid message", "A listener for the destination /teste/ already exists",ex.getMessage());

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

        consumerManager.addSubscription(consumer);


        BrokerAsyncConsumer asyncConsumer = consumerManager.removeSubscription(destinationType,destinationName);

        Assert.assertNotNull(asyncConsumer);

        Assert.assertSame(consumer,asyncConsumer);


        BrokerAsyncConsumer consumer2 = consumerManager.getConsumer(destinationType,destinationName);

        Assert.assertNull(consumer2);

    }

}
