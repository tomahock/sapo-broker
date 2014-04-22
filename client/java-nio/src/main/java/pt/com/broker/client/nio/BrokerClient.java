package pt.com.broker.client.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.consumer.BrokerAsyncConsumer;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.*;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClient {

    private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);

    private final String host;
    private final int port;

    private Bootstrap bootstrap;

    private ChannelFuture future;

    private NetProtocolType protocolType;


    private ConsumerManager consumerManager;


    public BrokerClient(String _host, int _port) {

        this(_host, _port, NetProtocolType.JSON);

    }
    public BrokerClient(String host, int port, NetProtocolType ptype) {

        this.host = host;
        this.port = port;

        setBootstrap(new Bootstrap());

        setProtocolType(ptype);

        setConsumerManager(new ConsumerManager());
    }

    public ChannelFuture connect(){

        EventLoopGroup group = new NioEventLoopGroup();

        getBootstrap().group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new BrokerMessageDecoder(getProtocolType()));
                            ch.pipeline().addLast(new BrokerMessageEncoder(getProtocolType()));

                            ch.pipeline().addLast(new ReceiveMessageHandler(consumerManager));

                        }
                    });

        return future = getBootstrap().connect();

    }

    public ChannelFuture enqueueMessage(String brokerMessage, String destinationName){

        NetBrokerMessage msg = new NetBrokerMessage(brokerMessage);

        return enqueueMessage(msg,destinationName);
    }

    public ChannelFuture enqueueMessage(NetBrokerMessage brokerMessage, String destinationName)
    {

        if ((brokerMessage == null) ||  StringUtils.isBlank(destinationName)){
            throw new IllegalArgumentException("Mal-formed Enqueue request");
        }


        NetPublish publish = new NetPublish(destinationName, NetAction.DestinationType.QUEUE, brokerMessage);

        NetAction action = new NetAction(NetAction.ActionType.PUBLISH);

        action.setPublishMessage(publish);

        return sendNetMessage(new NetMessage(action,brokerMessage.getHeaders()));

    }

    public ChannelFuture subscribe(NetSubscribe subscribe, BrokerListener listener){

        getConsumerManager().addSubscription(new BrokerAsyncConsumer(subscribe, listener));

        log.info("Created new async consumer for '{}'", subscribe.getDestination());

        listener.setBrokerClient(this);

        NetAction netAction = new NetAction(NetAction.ActionType.SUBSCRIBE);
        netAction.setSubscribeMessage(subscribe);

        NetMessage netMessage = buildMessage(netAction,subscribe.getHeaders());

        return sendNetMessage(netMessage);
    }



    public ChannelFuture acknowledge(NetNotification notification) throws Throwable
    {

        if( (notification==null ) || (notification.getMessage() == null) || StringUtils.isBlank(notification.getMessage().getMessageId())){
            throw new IllegalArgumentException("Can't acknowledge invalid message.");
        }

        /* there is no acknnowledge for topic  */
        if (notification.getDestinationType() == NetAction.DestinationType.TOPIC)
        {
            return null;
        }



        NetBrokerMessage brkMsg = notification.getMessage();
        String ackDestination = notification.getSubscription();
        NetAcknowledge ackMsg = new NetAcknowledge(ackDestination, brkMsg.getMessageId());

        NetAction action = new NetAction(NetAction.ActionType.ACKNOWLEDGE);
        action.setAcknowledgeMessage(ackMsg);

        NetMessage msg = buildMessage(action);

        return sendNetMessage(msg);

    }

    protected ChannelFuture sendNetMessage(NetMessage msg){

        return  getChannel().writeAndFlush(msg);

    }

    private NetMessage buildMessage(NetAction action, Map<String, String> headers)
    {
        NetMessage message = new NetMessage(action, headers);

        return message;
    }


    private NetMessage buildMessage(NetAction action)
    {
        return this.buildMessage(action,new HashMap<String, String>());
    }

    public Future close(){
        return getBootstrap().group().shutdownGracefully();
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }


    protected Channel getChannel(){

        try {

            this.future.sync();

        } catch (InterruptedException e) {

            throw new RuntimeException("Client was not able to connect");
        }

        return this.future.channel();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ChannelFuture getFuture() {
        return future;
    }

    public void setFuture(ChannelFuture future) {
        this.future = future;
    }

    public NetProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(NetProtocolType protocolType) {
        this.protocolType = protocolType;
    }


    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }

    public void setConsumerManager(ConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }
}

