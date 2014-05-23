package pt.com.broker.client.nio;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import io.netty.util.AttributeKey;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.bootstrap.BaseChannelInitializer;
import pt.com.broker.client.nio.bootstrap.Bootstrap;
import pt.com.broker.client.nio.bootstrap.ChannelInitializer;

import pt.com.broker.client.nio.bootstrap.DatagramChannelInitializer;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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

        channelInitializer  = new ChannelInitializer(getSerializer(), getConsumerManager(), getPongConsumerManager());


        channelInitializer.setOldFraming(getProtocolType() == NetProtocolType.SOAP_v0);

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

    public Future subscribe(NetSubscribeAction  subscribe, final BrokerListener listener) {
        return subscribe(subscribe,listener,null);
    }

    public Future subscribe(final NetSubscribeAction subscribe, final BrokerListener listener , AcceptRequest request) {

        NetAction netAction = null;

        if(subscribe instanceof NetPoll){
            netAction = new NetAction((NetPoll)subscribe);
        }

        if(subscribe instanceof NetSubscribe){
            netAction = new NetAction((NetSubscribe)subscribe);
        }


        if(request!=null) {
            subscribe.setActionId(request.getActionId());
            addAcceptMessageHandler(request);
        }


        NetMessage netMessage = buildMessage(netAction,subscribe.getHeaders());

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
    public Future unsubscribe(NetAction.DestinationType destinationType, String dstName){

        NetUnsubscribe unsubscribe = new NetUnsubscribe(dstName,destinationType);

        NetMessage netMessage = new NetMessage(new NetAction(unsubscribe));

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

        return sendNetMessage(msg,channel,null);

    }


    public Future checkStatus(final BrokerListener listener) throws Throwable {

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

    public NetMessage poll(String name) throws Exception {
        return poll(name,0);
    }

    public NetMessage poll(String name ,int timeout) throws Exception {

        NetPoll netPoll = new NetPoll(name, timeout);

        return this.poll(netPoll,null);
    }

    public NetMessage poll(final NetPoll netPoll, AcceptRequest request) throws Exception{

        if(request!=null){
            addAcceptMessageHandler(request);
            netPoll.setActionId(request.getActionId());
        }

        NetAction netAction = new NetAction(netPoll);

        final BlockingQueue<NetMessage> queue = new ArrayBlockingQueue<NetMessage>(1);


        try {

            subscribe(netPoll, new BrokerListenerAdapter() {

                @Override
                public boolean onMessage(NetMessage message) {


                    try {

                        queue.put(message);

                        return true;

                    } catch (InterruptedException e) {

                        e.printStackTrace();

                        return false;

                    }finally {
                        getConsumerManager().removeSubscription(netPoll);
                    }

                }

                @Override
                public void onFault(NetMessage message) {
                    onMessage(message);
                }
            });


            latch.await();

            return queue.take();

            return response[0];

        } catch (Throwable e) {

            throw new Exception("There was an unexpected error",e);

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

   public void setFaultListner(BrokerListener adapter){
        channelInitializer.setFaultHandler(adapter);
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

