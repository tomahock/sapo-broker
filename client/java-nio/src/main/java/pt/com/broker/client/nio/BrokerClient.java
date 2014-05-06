package pt.com.broker.client.nio;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;

import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;

import pt.com.broker.client.nio.utils.HostContainer;
import pt.com.broker.types.*;


import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClient extends BaseClient {

    private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);

    private ConsumerManager consumerManager;

    private PongConsumerManager pongConsumerManager;

    public BrokerClient(NetProtocolType ptype) {
        super(ptype);
    }

    public BrokerClient(String host, int port) {
        super(host, port);
    }

    public BrokerClient(String host, int port, NetProtocolType ptype) {
        super(host, port, ptype);
    }

    public BrokerClient(HostInfo host, NetProtocolType ptype) {
        super(host, ptype);
    }


    protected void init(NetProtocolType ptype){

        setPongConsumerManager(new PongConsumerManager());
        setConsumerManager(new ConsumerManager());

        setBootstrap(new Bootstrap(new ChannelInitializer(ptype, getConsumerManager(), getPongConsumerManager())));

        setHosts(new HostContainer(getBootstrap()));
    }


    public ChannelFuture publishMessage(String brokerMessage, String destinationName,NetAction.DestinationType dtype) {

        return publishMessage(brokerMessage.getBytes(), destinationName, dtype);
    }

    public ChannelFuture publishMessage(byte[] brokerMessage, String destinationName , NetAction.DestinationType dtype) {

        NetBrokerMessage msg = new NetBrokerMessage(brokerMessage);

        return publishMessage(msg, destinationName, dtype);
    }

    public ChannelFuture publishMessage(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype) {

        if ((brokerMessage == null) || StringUtils.isBlank(destination)) {
            throw new IllegalArgumentException("Mal-formed Enqueue request");
        }


        NetPublish publish = new NetPublish(destination, dtype, brokerMessage);

        NetAction action = new NetAction(publish);


        return sendNetMessage(new NetMessage(action, brokerMessage.getHeaders()));

    }


    public ChannelFuture subscribe(NetSubscribe subscribe, BrokerListener listener) {

        getConsumerManager().addSubscription(subscribe, listener);

        log.info("Created new async consumer for '{}'", subscribe.getDestination());

        listener.setBrokerClient(this);

        NetAction netAction = new NetAction(subscribe);

        NetMessage netMessage = buildMessage(netAction, subscribe.getHeaders());

        return sendNetMessage(netMessage);
    }

    public ChannelFuture acknowledge(NetNotification notification) throws Throwable {

        return  this.acknowledge(notification,null);

    }

    public ChannelFuture acknowledge(NetNotification notification, Channel channel) throws Throwable {
        /* there is no acknowledge action for topics  */
        if (notification.getDestinationType() == NetAction.DestinationType.TOPIC) {
            return null;
        }

        if ((notification == null) || (notification.getMessage() == null) || StringUtils.isBlank(notification.getMessage().getMessageId())) {
            throw new IllegalArgumentException("Can't acknowledge invalid message.");
        }


        NetBrokerMessage brkMsg = notification.getMessage();
        String ackDestination = notification.getSubscription();

        String msgid = brkMsg.getMessageId();

        String[] parts = msgid.split("#",2);

        if(parts.length > 1){
            msgid = parts[1];
        }

        NetAcknowledge ackMsg = new NetAcknowledge(ackDestination, msgid);

        NetAction action = new NetAction(ackMsg);

        NetMessage msg = buildMessage(action);

        return sendNetMessage(msg,channel);

    }


    public ChannelFuture checkStatus(BrokerListener listener) throws Throwable {

        String actionId = UUID.randomUUID().toString();

        NetPing ping = new NetPing(actionId);


        getPongConsumerManager().addSubscription(ping,listener);


        NetAction action = new NetAction(ping);

        NetMessage message = buildMessage(action);


        ChannelFuture f = sendNetMessage(message);

        return f;
    }



    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }

    public void setConsumerManager(ConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }

    public PongConsumerManager getPongConsumerManager() {
        return pongConsumerManager;
    }

    public void setPongConsumerManager(PongConsumerManager pongConsumerManager) {
        this.pongConsumerManager = pongConsumerManager;
    }

    public HostContainer getHosts() {
        return hosts;
    }


}

