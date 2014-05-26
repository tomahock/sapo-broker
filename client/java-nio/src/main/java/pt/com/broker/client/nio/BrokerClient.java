package pt.com.broker.client.nio;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

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

import pt.com.broker.client.nio.events.MessageAcceptedListener;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.handlers.timeout.TimeoutException;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.client.nio.server.ReconnectEvent;
import pt.com.broker.client.nio.utils.NetNotificationDecorator;
import pt.com.broker.types.*;


import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClient extends BaseClient implements Observer {

    private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);

    private ConsumerManager consumerManager;

    private PongConsumerManager pongConsumerManager;

    private PendingAcceptRequestsManager acceptRequestsManager;

    private ChannelInitializer channelInitializer;


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
        return subscribe(subscribe, listener, null);
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


        NetMessage netMessage = buildMessage(netAction, subscribe.getHeaders());

        final BrokerClient client = this;

        return sendNetMessage(netMessage, new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if (future.isSuccess()) {

                    log.info("Created new async consumer for '{}'", subscribe.getDestination());

                    HostInfo host = (HostInfo) future.channel().attr(HostContainer.ATTRIBUTE_HOST_INFO).get();

                    getConsumerManager().addSubscription(subscribe, listener, host);

                    /* @todo ver o auto acknolage */
                    listener.setBrokerClient(client);

                } else {
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


    /**
     * Acknowledge and NetNotification received from the server. This method should only be used
     * in
     *
     *
     * @param notification
     * @return
     * @throws Throwable
     */
    public ChannelFuture acknowledge(NetNotification notification) throws Throwable {

        if(!(notification instanceof NetNotificationDecorator)){
            throw new Exception("Invalid NetNotification");
        }

        Channel channel = ((NetNotificationDecorator) notification).getChannel();

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
                }else{
                    throw new Exception("Was not possible to check Status");
                }

            }
        });

        return f;
    }

    /**
     * @see pt.com.broker.client.nio.BrokerClient#poll(pt.com.broker.types.NetPoll, AcceptRequest)
     *
     * @param name
     * @return
     */
    public NetNotification poll(String name) {

        try {

            return poll(name, 0);

        } catch (TimeoutException e) {

            //there is no timeout exception

            throw  new RuntimeException(e);
        }

    }

    /**
     *
     * @see pt.com.broker.client.nio.BrokerClient#poll(pt.com.broker.types.NetPoll, AcceptRequest)
     *
     * @param name
     * @param timeout
     * @return
     * @throws TimeoutException
     */
    public NetNotification poll(String name ,int timeout) throws TimeoutException {

        NetPoll netPoll = new NetPoll(name, timeout);

        return this.poll(netPoll,null);
    }

    /**
     *  Blocks until a message is received.
     *
     * @param netPoll
     * @param request
     * @return
     * @throws TimeoutException
     *
     */
    public NetNotification poll(final NetPoll netPoll, AcceptRequest request) throws TimeoutException{

        if(request!=null){
            addAcceptMessageHandler(request);
            netPoll.setActionId(request.getActionId());
        }


        final BlockingQueue<NetNotification> queue = new ArrayBlockingQueue<NetNotification>(1);


        try {

            subscribe(netPoll, new NotificationListenerAdapter() {


                @Override
                public void deliverMessage(NetMessage message, Channel channel) throws Throwable {

                    if(message.getAction().getActionType().equals(NetAction.ActionType.FAULT)
                            && message.getAction().getFaultMessage().getCode().equals(NetFault.PollTimeoutErrorCode)){

                        throw new TimeoutException("Poll timeout");

                    }

                        super.deliverMessage(message, channel);
                }

                @Override
                public boolean onMessage(NetNotification message) {

                    try {

                        queue.put(message);

                        return false;

                    } catch (InterruptedException e) {

                        throw new RuntimeException(e);

                    }finally {
                        getConsumerManager().removeSubscription(netPoll);
                    }

                }


            });




            return queue.take();


        } catch (Throwable e) {

            throw new RuntimeException("There was an unexpected error",e);

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

   public void setFaultListener(BrokerListener adapter){
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

