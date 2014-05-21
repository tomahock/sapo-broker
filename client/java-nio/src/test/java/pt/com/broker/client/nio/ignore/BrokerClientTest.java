package pt.com.broker.client.nio.ignore;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.AcceptRequest;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.events.BrokerListenerAdapter;
import pt.com.broker.client.nio.events.MessageAcceptedAdapter;
import pt.com.broker.client.nio.events.PongListenerAdapter;
import pt.com.broker.types.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClientTest {


    private static final Logger log = LoggerFactory.getLogger(BrokerClientTest.class);

    @Test
    public void testClientConnect(){

        BrokerClient bk = new BrokerClient("localhost",3323);

        bk.addServer("localhost",3323);
        bk.addServer("localhost",3324);
        bk.addServer("localhost",3323);
        bk.addServer("localhost",3323);


        HostInfo f = bk.connect();


        log.debug("Connected to 1 Host");

        //Thread.sleep(5000);

    }

    @Test
    public void testClientConnectAsync() throws ExecutionException, InterruptedException {

        BrokerClient bk = new BrokerClient("localhost",3323);

        bk.addServer("localhost",3323);
        bk.addServer("localhost",3324);
        bk.addServer("localhost",3323);
        bk.addServer("localhost",3323);


        Future<HostInfo> f = bk.connectAsync();


        f.get();


        log.debug("Connected to 1 Host");

        //Thread.sleep(5000);

    }


    @Test
    public void testEncoding() throws Exception{

        EmbeddedChannel channel = null;



        channel = new EmbeddedChannel(new BrokerMessageEncoder(BindingSerializerFactory.getInstance(NetProtocolType.JSON)));

        NetMessage nmsg = createNewMessage();

        Assert.assertTrue(channel.writeOutbound(nmsg));

        ByteBuf out = (ByteBuf) channel.readOutbound();

        log.debug(ByteBufUtil.hexDump(out));

        byte[] data = new byte[out.readableBytes()];

        out.readBytes(data);

        Assert.assertTrue(data.length > 0);




    }


    @Test
    public void testDecoding()  throws Exception{

        EmbeddedChannel channel = null;

        channel = new EmbeddedChannel(new BrokerMessageDecoder(BindingSerializerFactory.getInstance(NetProtocolType.JSON)));

        NetMessage net = createNewMessage();

        byte[] data = MessageToByte(net);

        ByteBuf buf =  Unpooled.buffer();

        buf.writeBytes(data);

        channel.writeInbound(buf);

        channel.finish();

        NetMessage msg = (NetMessage)channel.readInbound();

        Assert.assertTrue(msg instanceof  NetMessage);

        log.debug(new String(msg.getAction().getPublishMessage().getMessage().getPayload(), "UTF-8"));

    }


    protected NetMessage createNewMessage(){


        NetAction action = new NetAction(NetAction.ActionType.PUBLISH);

        NetBrokerMessage msg = new NetBrokerMessage(new String("Olá Mundo"));

        NetPublish publish = new NetPublish("/teste/", NetAction.DestinationType.QUEUE, msg);

        action.setPublishMessage(publish);

        NetMessage nmsg = new NetMessage(action);

        return nmsg;
    }


    protected byte[] MessageToByte(NetMessage nmsg) throws Exception{

        EmbeddedChannel channel = null;

        channel = new EmbeddedChannel(new BrokerMessageEncoder(BindingSerializerFactory.getInstance(NetProtocolType.JSON)));

        Assert.assertTrue(channel.writeOutbound(nmsg));

        ByteBuf out = (ByteBuf) channel.readOutbound();

        byte[] data = new byte[out.readableBytes()];

        out.readBytes(data);

        return data;
    }


    @Test
    public void testClientEnqueueMessage() throws Exception{

        BrokerClient bk = new BrokerClient("192.168.100.1",3323);

        log.debug("connecting....");
        Future<HostInfo> f = bk.connectAsync();


        log.debug("Waiting for connection....");
        f.get();
        log.debug("Connected....");

        log.debug("sending message....");
        ChannelFuture future = bk.publishMessage("Olá Mundo", "/teste/", NetAction.DestinationType.QUEUE);



        log.debug("waiting for message be delivered....");
        future.sync();


    }

    @Test
    public void testSubscribe() throws Exception {


        BrokerClient bk = new BrokerClient("localhost",3323);

        Future<HostInfo> f= bk.connectAsync();

        try {

            f.get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        log.debug("Subscribe");


        Future fs = bk.subscribe("/teste/",NetAction.DestinationType.QUEUE,new BrokerListenerAdapter() {

            @Override
            public boolean onMessage(NetMessage message) {

                // do something

                return true; // return true or false to acknowledge or not
            }

        });


        try {
            fs.get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testSubscribeAndReceive() throws Throwable{

        this.testClientEnqueueMessage();

        BrokerClient bk = new BrokerClient("192.168.100.1",3323,NetProtocolType.JSON);



        Future<HostInfo> f = bk.connectAsync();

        try {
            f.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        NetSubscribe netSubscribe = new NetSubscribe("/teste/", NetAction.DestinationType.QUEUE);


        log.debug("Subscribe");

        Future fs = bk.subscribe(netSubscribe,new BrokerListenerAdapter() {

            @Override
            public boolean onMessage(NetMessage message) {

                try {

                    log.debug(message.getAction().getNotificationMessage().getMessage().getMessageId());
                    log.debug(new String(message.getAction().getNotificationMessage().getMessage().getPayload(),"UTF-8"));

                    for(Map.Entry<String, String> entry : message.getHeaders().entrySet()){
                        System.out.println(entry.getKey() + "/" + entry.getValue());
                    }

                    System.out.println("---------------------------------");

                    if(message.getAction().getNotificationMessage().getHeaders() != null) {
                        for (Map.Entry<String, String> entry : message.getAction().getNotificationMessage().getHeaders().entrySet()) {
                            System.out.println(entry.getKey() + "/" + entry.getValue());
                        }
                    }

                    return true;

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            public void onFault(NetMessage message) {

            }
        });

        try {
            fs.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        //ChannelFuture future = bk.publishMessage("Olá Mundo", "/teste/", NetAction.DestinationType.QUEUE);

        /*future = bk.publishMessage("Olá Mundo", "/teste/", NetAction.DestinationType.QUEUE);
        future = bk.publishMessage("Olá Mundo", "/teste/", NetAction.DestinationType.QUEUE);
        future = bk.publishMessage("Olá Mundo", "/teste/", NetAction.DestinationType.QUEUE);*/


        Thread.sleep(10000);

    }


    @Test
    public void testConnectionError() throws Throwable{

        this.testClientEnqueueMessage();

        BrokerClient bk = new BrokerClient("localhost",3323);



        Future<HostInfo> f = bk.connectAsync();

        try {

            f.get();

        } catch (InterruptedException e) {

            e.printStackTrace();

        }


        NetAction.DestinationType destinationType = NetAction.DestinationType.QUEUE;

        int i = 1000;

        while (--i > 0){

            ChannelFuture future = bk.publishMessage("Olá Mundo", "/teste/", destinationType);



            Thread.sleep(1000);
        }



        Thread.sleep(5000);

    }

    @Test
    public void testDeferedDelivery() throws Throwable {


        BrokerClient bk = new BrokerClient("192.168.100.1", 3323,NetProtocolType.JSON);


        NetBrokerMessage brokerMessage = new NetBrokerMessage("teste");

        // Specify the delivery interval (in milliseconds)
        brokerMessage.addHeader(Headers.DEFERRED_DELIVERY, "1000" );


        ChannelFuture f = bk.publishMessage(brokerMessage, "/teste/", NetAction.DestinationType.QUEUE);


        f.get();


    }


    @Test
    public void testPingPong() throws Throwable {

        BrokerClient bk = new BrokerClient("192.168.100.1", 3323,NetProtocolType.JSON);

        Future f = bk.connectAsync();

        f.get();



        bk.checkStatus(new PongListenerAdapter() {
            @Override
            public void onMessage(NetPong message) {

                log.debug("Got pong message");

            }

        });


        Thread.sleep(10000);

    }

    @Test
    public void testDeferedDeliveryOldFrame() throws Throwable {


        BrokerClient bk = new BrokerClient("192.168.100.1", 3322,NetProtocolType.SOAP_v0);



        NetBrokerMessage brokerMessage = new NetBrokerMessage("teste");


        ChannelFuture f = bk.publishMessage(brokerMessage, "/teste/", NetAction.DestinationType.QUEUE);


        f.get();


    }


    @Test
    public void testPool() throws Throwable{

        BrokerClient bk = new BrokerClient("192.168.100.1", 3323,NetProtocolType.JSON);



        int counter = 10;

        while (counter-- > 0){

            NetMessage netMessage = bk.poll("/teste/", 2000);

            System.out.println("Action Type:" + netMessage.getAction().getActionType().name());
        }

    }


    @Test
    public void testAcceptMessage() throws InterruptedException {

        BrokerClient bk = new BrokerClient("192.168.100.1", 3323,NetProtocolType.JSON);

        NetBrokerMessage message = new NetBrokerMessage("teste");

        AcceptRequest acceptRequest = new AcceptRequest(UUID.randomUUID().toString(),new MessageAcceptedAdapter(){

            @Override
            public boolean onMessage(NetMessage message) {
                System.out.println("Message: "+message.getAction().getAcceptedMessage().getActionId());

                return true;
            }

            @Override
            public void onFault(NetMessage message) {
                System.out.println("Fault: "+message.getAction().getFaultMessage().getDetail());
            }

            @Override
            public void onTimeout(String actionID) {
                System.out.println("Timeout: "+actionID);
            }
        },2000);

        bk.publishMessage(message,"/teste/", NetAction.DestinationType.QUEUE, acceptRequest);

        Thread.sleep(10000);
    }

}
