package pt.com.broker.client.nio.ignore;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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
import pt.com.broker.client.nio.events.*;
import pt.com.broker.client.nio.handlers.timeout.TimeoutException;
import pt.com.broker.types.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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

        BrokerClient bk = new BrokerClient("192.168.100.1",3323, NetProtocolType.JSON);

        log.debug("connecting....");
        Future<HostInfo> f = bk.connectAsync();


        log.debug("Waiting for connection....");
        f.get();
        log.debug("Connected....");



         bk.subscribe("/teste/", NetAction.DestinationType.QUEUE,new NotificationListenerAdapter() {
             @Override
             public boolean onMessage(NetNotification message) {

                 System.out.println("Message");

                 return true;
             }
         }).get();

        log.debug("sending message....");
        NetBrokerMessage netBrokerMessage = new NetBrokerMessage("Teste2");
        netBrokerMessage.setExpiration(System.currentTimeMillis()-30000);

        ChannelFuture future = bk.publishMessage(netBrokerMessage, "/teste/", NetAction.DestinationType.QUEUE);
        //ChannelFuture future = bk.publishMessage("Teste3", "/teste/", NetAction.DestinationType.QUEUE);



        log.debug("waiting for message be delivered....");
        future.sync();


        Thread.sleep(10000);


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


        Future fs = bk.subscribe("/teste/",NetAction.DestinationType.QUEUE,new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification message) {

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

        Future fs = bk.subscribe(netSubscribe,new NotificationListenerAdapter() {

            @Override
            public boolean onMessage(NetNotification message) {

                try {

                    log.debug(message.getMessage().getMessageId());
                    log.debug(new String(message.getMessage().getPayload(),"UTF-8"));

                    for(Map.Entry<String, String> entry : message.getHeaders().entrySet()){
                        System.out.println(entry.getKey() + "/" + entry.getValue());
                    }

                    System.out.println("---------------------------------");

                    if(message.getHeaders() != null) {
                        for (Map.Entry<String, String> entry : message.getHeaders().entrySet()) {
                            System.out.println(entry.getKey() + "/" + entry.getValue());
                        }
                    }

                    return true;

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return false;
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


            try{

                NetNotification netNotification = bk.poll("/teste/", 2000);

                System.out.println("Notification: " + netNotification);

            }catch (TimeoutException e){

                // there was a timeout

            }



        }

    }


    @Test
    public void testAcceptMessage() throws InterruptedException {

        BrokerClient bk = new BrokerClient("192.168.100.1", 3323,NetProtocolType.JSON);

        NetBrokerMessage message = new NetBrokerMessage("teste");

        AcceptRequest acceptRequest = new AcceptRequest(UUID.randomUUID().toString(),new MessageAcceptedAdapter(){

            @Override
            public void onMessage(NetAccepted message,HostInfo host) {
                System.out.println("Message: " + message.getActionId());

            }

            @Override
            public void onFault(NetFault message,HostInfo host) {
                System.out.println("Fault: " + message.getDetail());
            }

            @Override
            public void onTimeout(String actionID) {
                System.out.println("Timeout: "+actionID);
            }
        },2000);

        bk.publishMessage(message,"/teste/", NetAction.DestinationType.QUEUE, acceptRequest);

        Thread.sleep(10000);
    }

    @Test()
    public void testMultipleSubscribe() throws InterruptedException {


        final BrokerClient bk = new BrokerClient(NetProtocolType.JSON);

        bk.addServer("127.0.0.1" , 3323);
        bk.addServer("127.0.0.1" , 3423);

        bk.connect();

        bk.subscribe("/teste/",NetAction.DestinationType.QUEUE,new BrokerListener() {


            @Override
            public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

                bk.acknowledge(message.getAction().getNotificationMessage());
            }

            @Override
            public void setBrokerClient(BrokerClient client) {

            }
        });


        Thread.sleep(10000);

    }

    @Test()
    public void testVirtualQueue() throws InterruptedException, ExecutionException {


        final BrokerClient bk = new BrokerClient(NetProtocolType.JSON);

        bk.addServer("192.168.100.1" , 3323);


        bk.connect();




        final AtomicInteger counter = new AtomicInteger(3);

        final Future f = bk.subscribe("localhost@/teste/", NetAction.DestinationType.VIRTUAL_QUEUE ,new NotificationListenerAdapter(){

            @Override
            public boolean onMessage(NetNotification notification) {

                if(counter.getAndDecrement() > 1){

                    System.out.println("Got a Message. Not Ackcepted");

                    return false;
                }else{

                    System.out.println("Got a Message and ackcepted");

                    return true;
                }



            }

        });

        f.get();


         bk.publishMessage("teste","/teste/", NetAction.DestinationType.TOPIC);


        Thread.sleep(100000);

    }

}
