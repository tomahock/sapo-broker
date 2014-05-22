package pt.com.broker.client.nio;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import io.netty.util.AttributeKey;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;

import pt.com.broker.client.nio.consumer.BrokerAsyncConsumer;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;

import pt.com.broker.client.nio.events.BrokerListenerAdapter;
import pt.com.broker.client.nio.events.MessageAcceptedListener;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.client.nio.server.ReconnectEvent;
import pt.com.broker.types.*;


import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClient extends BaseClient implements Observer {

    private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);

    private ConsumerManager consumerManager;

    private PongConsumerManager pongConsumerManager;

    private PendingAcceptRequestsManager acceptRequestsManager;




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


    protected void init(){

        setPongConsumerManager(new PongConsumerManager());
        setConsumerManager(new ConsumerManager());

        ChannelInitializer channelInitializer  = new ChannelInitializer(getSerializer(), getConsumerManager(), getPongConsumerManager());

        setBootstrap(new Bootstrap(channelInitializer));

        setAcceptRequestsManager(new PendingAcceptRequestsManager(getBootstrap().getGroup()));

        channelInitializer.setAcceptRequestsManager(getAcceptRequestsManager());

        HostContainer hostContainer = new HostContainer(getBootstrap());

        hostContainer.addObserver(this);

        setHosts(hostContainer);
    }


    public ChannelFuture publishMessage(String brokerMessage, String destinationName,NetAction.DestinationType dtype) {

        return publishMessage(brokerMessage.getBytes(), destinationName, dtype);
    }

    public ChannelFuture publishMessage(byte[] brokerMessage, String destinationName , NetAction.DestinationType dtype) {

        NetBrokerMessage msg = new NetBrokerMessage(brokerMessage);

        return publishMessage(msg, destinationName, dtype);
    }

    public ChannelFuture publishMessage(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype) {
        return publishMessage(brokerMessage,destination,dtype,null);
    }

    public ChannelFuture publishMessage(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype , AcceptRequest request) {


        if ((brokerMessage == null) || StringUtils.isBlank(destination)) {
            throw new IllegalArgumentException("Mal-formed Enqueue request");
        }

        NetPublish publish = new NetPublish(destination, dtype, brokerMessage);

        if(request!=null){
            publish.setActionId(request.getActionId());
            addAcceptMessageHandler(request);
        }

        NetAction action = new NetAction(publish);


        return sendNetMessage(new NetMessage(action, brokerMessage.getHeaders()));

    }

    public Future subscribe(String destination, NetAction.DestinationType destinationType, final BrokerListener listener) {
        return subscribe( new NetSubscribe(destination, destinationType),listener,null);
    }

    public Future subscribe(final NetSubscribe subscribe, final BrokerListener listener) {
        return subscribe(subscribe,listener,null);
    }

    public Future subscribe(final NetSubscribe subscribe, final BrokerListener listener , AcceptRequest request) {


        NetAction netAction = new NetAction(subscribe);



        if(request!=null) {
            subscribe.setActionId(request.getActionId());
            addAcceptMessageHandler(request);
        }


        NetMessage netMessage = buildMessage(netAction, subscribe.getHeaders());

        return sendNetMessage(netMessage, new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if (future.isSuccess()) {

                    log.info("Created new async consumer for '{}'", subscribe.getDestination());

                    HostInfo host = (HostInfo) future.channel().attr(HostContainer.ATTRIBUTE_HOST_INFO).get();

                    getConsumerManager().addSubscription(subscribe, listener, host);

                    /* @todo ver o auto acknolage */
                    //listener.setBrokerClient(this);

                }else{
                    log.debug("Error creating async consumer");
                }

            }
        });
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

        return sendNetMessage(msg,channel,null);

    }


    public ChannelFuture checkStatus(final BrokerListener listener) throws Throwable {

        String actionId = UUID.randomUUID().toString();

        final NetPing ping = new NetPing(actionId);

        NetAction action = new NetAction(ping);

        NetMessage message = buildMessage(action);


        final ChannelFuture f = sendNetMessage(message, new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if(future.isSuccess()){
                    getPongConsumerManager().addSubscription(ping,listener);
                }

            }
        });

        return f;
    }

    public NetMessage poll(String name){
        return poll(name,0);
    }

    public NetMessage poll(String name ,int timeout){

        NetPoll netPoll = new NetPoll(name, timeout);

        return this.poll(netPoll,null);
    }

    public NetMessage poll(final NetPoll netPoll, AcceptRequest request){


        if(request!=null){
            addAcceptMessageHandler(request);
            netPoll.setActionId(request.getActionId());
        }

        NetAction netAction = new NetAction(netPoll);

        NetMessage netMessage = buildMessage(netAction);


        final CountDownLatch latch = new CountDownLatch(1);

        final NetMessage[] response = {null};

        ChannelFuture f = sendNetMessage(netMessage);


        try {

            f.get();

            if(!f.isSuccess()){
                return null;
            }

            getConsumerManager().addSubscription(netPoll, new BrokerListenerAdapter() {
                @Override
                public boolean onMessage(NetMessage message) {

                    response[0] = message;
                    latch.countDown();

                    return true;
                }

                @Override
                public void onFault(NetMessage message) {
                    onMessage(message);
                }
            });


            latch.await();

            getConsumerManager().removeSubscription(netPoll.getDestinationType(),netPoll.getDestination());

            return response[0];


        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }


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

    public PendingAcceptRequestsManager getAcceptRequestsManager() {
        return acceptRequestsManager;
    }

    public void setAcceptRequestsManager(PendingAcceptRequestsManager acceptRequestsManager) {
        this.acceptRequestsManager = acceptRequestsManager;
    }


    protected void addAcceptMessageHandler(AcceptRequest request){

        String actionID = request.getActionId();
        long timeout = request.getTimeout();
        MessageAcceptedListener listener = request.getListener();

        try {
            getAcceptRequestsManager().addAcceptRequest(actionID, timeout, listener);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }


    /**
     * Every time a host reconnect the ConsumerManager is notified
     *
     * @param observable
     * @param o
     */
    @Override
    public void update(Observable observable, Object o) {


        if(observable instanceof HostContainer && o instanceof ReconnectEvent){

                HostInfo host = ((ReconnectEvent) o).getHost();

                resubscribe(host);
        }

    }


    private void resubscribe(HostInfo host){

        log.debug("Resubscribing : "+host);

        Map<String,BrokerAsyncConsumer> map =  consumerManager.removeSubscriptions(NetAction.DestinationType.QUEUE, host);

        for(Map.Entry<String, BrokerAsyncConsumer> entry : map.entrySet() ){
            BrokerListener listener = entry.getValue().getListener();

            log.debug("Destination: "+entry.getKey());
            this.subscribe(entry.getKey(),NetAction.DestinationType.QUEUE,listener);
        }


    }




}

