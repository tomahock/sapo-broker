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


import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.handlers.timeout.TimeoutException;
import pt.com.broker.client.nio.server.HostContainer;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.server.ReconnectEvent;
import pt.com.broker.client.nio.utils.ChannelDecorator;
import pt.com.broker.client.nio.utils.NetNotificationDecorator;
import pt.com.broker.types.*;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClient extends BaseClient implements Observer {

    private static final Logger log = LoggerFactory.getLogger(BrokerClient.class);

    private ConsumerManager consumerManager;

    private PongConsumerManager pongConsumerManager;

    private PendingAcceptRequestsManager acceptRequestsManager;

    private ChannelInitializer channelInitializer;


    ExecutorService executorService = Executors.newFixedThreadPool(10);

    private final CompletionService<HostInfo> service = new ExecutorCompletionService<HostInfo>(executorService);
    private final CompletionService<HostInfo> unsubcribeService = new ExecutorCompletionService<HostInfo>(executorService);


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


    public Future publish(String brokerMessage, String destinationName, NetAction.DestinationType dtype) {

        return publish(brokerMessage.getBytes(), destinationName, dtype);
    }

    public Future publish(byte[] brokerMessage, String destinationName, NetAction.DestinationType dtype) {

        NetBrokerMessage msg = new NetBrokerMessage(brokerMessage);

        return publish(msg, destinationName, dtype);
    }

    public Future publish(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype) {
        return publish(brokerMessage, destination, dtype, null);
    }

    public Future publish(NetBrokerMessage brokerMessage, String destination, NetAction.DestinationType dtype, AcceptRequest request) {


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

    public Future subscribe(String destination, NetAction.DestinationType destinationType, final BrokerListener listener) throws InterruptedException {
        return subscribe( new NetSubscribe(destination, destinationType),listener,null);
    }

    public Future subscribe(NetSubscribeAction  subscribe, final BrokerListener listener) throws InterruptedException {
        return subscribe(subscribe, listener, null);
    }

    public Future<HostInfo> subscribe(final NetSubscribeAction subscribe, final BrokerListener listener , final AcceptRequest request) throws InterruptedException {


        Collection<HostInfo> servers  = null;

        if(subscribe.getDestinationType() == NetAction.DestinationType.TOPIC){
            servers = new ArrayList<HostInfo>();

            servers.add( getAvailableHost());
        }else{
            servers = getHosts().getConnectedHosts();
        }




        if(request!=null) {
            subscribe.setActionId(request.getActionId());
            addAcceptMessageHandler(request);
        }

        for(final HostInfo host : servers){

            service.submit(new Callable<HostInfo>() {

                @Override
                public HostInfo call() throws Exception {

                    ChannelFuture future = subscribeToHost(subscribe,listener,host);

                    final CountDownLatch latch = new CountDownLatch(1);

                    future.addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                                latch.countDown();
                        }
                    });


                    latch.await();


                    return host;

                }
            });


        }


        return service.take();


    }

    private ChannelFuture subscribeToHost(final NetSubscribeAction subscribe , final BrokerListener listener){
            return subscribeToHost(subscribe,listener,getAvailableHost());
    }


    private ChannelFuture subscribeToHost(final NetSubscribeAction subscribe , BrokerListener listener , final HostInfo host){

        if(listener == null){
            throw new IllegalArgumentException("Invalid Listener");
        }

        if(subscribe.getDestinationType() == NetAction.DestinationType.VIRTUAL_QUEUE){

            String destination = subscribe.getDestination();

            if(!destination.contains("@")){
                throw new IllegalArgumentException("Invalid name format for virtual queue");
            }

        }

        NetAction netAction = null;

        if(subscribe instanceof NetPoll){
            netAction = new NetAction((NetPoll)subscribe);
        }

        if(subscribe instanceof NetSubscribe){
            netAction = new NetAction((NetSubscribe)subscribe);
        }

        final NetMessage netMessage = buildMessage(netAction, subscribe.getHeaders());


        getConsumerManager().addSubscription(subscribe, listener, host);

        if(listener instanceof NotificationListenerAdapter){
            ((NotificationListenerAdapter)listener).setBrokerClient(this);
        }

        ChannelFuture f =  sendNetMessage(netMessage,host);


        f.addListener(new ChannelFutureListener() {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if (!future.isSuccess()) {

                    getConsumerManager().removeSubscription(subscribe,host);

                    log.debug("Error creating async consumer",future.cause());
                }

            }

        });


        return f;

    }

    public Future unsubscribe(final NetAction.DestinationType destinationType, final String dstName)  throws InterruptedException {


        Map<String,BrokerAsyncConsumer> consumers = getConsumerManager().getSubscriptions(destinationType);


        int total_unsubscribe = 0;
        for(Map.Entry<String,BrokerAsyncConsumer> entry: consumers.entrySet()){

            BrokerAsyncConsumer consumer = entry.getValue();


            if(consumer.getDestinationName().equals(dstName) && consumer.getHost()!=null){

                final HostInfo host = consumer.getHost();

                total_unsubscribe++;

                unsubcribeService.submit(new Callable<HostInfo>() {
                    @Override
                    public HostInfo call() throws Exception {

                        ChannelFuture f = unsubscribeHost(destinationType,dstName,host);

                        final CountDownLatch latch = new CountDownLatch(1);

                        f.addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture future) throws Exception {

                                latch.countDown();

                            }
                        });

                        latch.await();

                        return  host;

                    }
                });

            }

        }


        if(total_unsubscribe  == 0){
            throw new IllegalArgumentException("No subscriptions found");
        }

        return unsubcribeService.take();

    }

    private ChannelFuture unsubscribeHost(final NetAction.DestinationType destinationType, final String dstName, final HostInfo host){

        NetUnsubscribe unsubscribe = new NetUnsubscribe(dstName,destinationType);

        NetMessage netMessage = new NetMessage(new NetAction(unsubscribe));


        ChannelFuture f = sendNetMessage(netMessage,host);

        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if(future.isSuccess()){

                    getConsumerManager().removeSubscription(destinationType,dstName,host);

                }else{

                    log.error("was not possible to unsubscribe",future.cause());

                }

            }
        });


        return f;
    }


    public Future acknowledge(NetNotification notification, HostInfo host) throws Throwable {

        if(!(notification instanceof NetNotificationDecorator)){
            throw new Exception("Invalid NetNotification");
        }

         /* there is no acknowledge action for topics  */
        if (notification.getDestinationType() == NetAction.DestinationType.TOPIC) {
            return null;
        }

        if ((notification.getMessage() == null) || StringUtils.isBlank(notification.getMessage().getMessageId())) {
            throw new IllegalArgumentException("Can't acknowledge invalid message.");
        }


        NetBrokerMessage brkMsg = notification.getMessage();
        String ackDestination = notification.getSubscription();

        String msgid = brkMsg.getMessageId();

        NetAcknowledge ackMsg = new NetAcknowledge(ackDestination, msgid);

        NetAction action = new NetAction(ackMsg);

        NetMessage msg = buildMessage(action);

        return sendNetMessage(msg,host);


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
    public Future acknowledge(NetNotification notification) throws Throwable {


        HostInfo host = ((NetNotificationDecorator) notification).getHost();

        return acknowledge(notification,host);

    }


    public Future checkStatus(final BrokerListener listener) throws Throwable {

        final String actionId = UUID.randomUUID().toString();

        NetPing ping = new NetPing(actionId);

        NetAction action = new NetAction(ping);

        NetMessage message = buildMessage(action);

        getPongConsumerManager().addSubscription(actionId,listener);

        final ChannelFuture f = sendNetMessage(message);

        f.addListener( new ChannelFutureListener(){

            @Override
            public void operationComplete(ChannelFuture future) throws Exception {

                if(!future.isSuccess()){

                    getPongConsumerManager().removeSubscription(actionId);

                    log.error("Was not possible to check Status",future.cause());
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

        final AtomicBoolean timeout = new AtomicBoolean(false);

        final CountDownLatch latch = new CountDownLatch(1);

        final List<NetNotification> notifications = new ArrayList<>(1);


        try {

            subscribeToHost(netPoll, new BrokerListener() {


                @Override
                public void deliverMessage(NetMessage message, HostInfo host) throws Throwable {

                    try {

                        NetFault netFault = message.getAction().getFaultMessage();


                        if(netFault!=null){

                            if (netFault.getCode().equals(NetFault.PollTimeoutErrorCode)) {
                                timeout.set(true);
                            }

                            return;
                        }


                        NetNotification netNotification = message.getAction().getNotificationMessage();

                        notifications.add(netNotification);




                    } catch (Exception e) {

                        throw e;

                    }
                    finally {

                        getConsumerManager().removeSubscription(netPoll, host);

                        latch.countDown();
                    }


                }


            });

            latch.await();


        } catch (Throwable e) {

            throw new RuntimeException("There was an unexpected error",e);

        }

        if(timeout.get()){
            throw new TimeoutException("Poll timeout");
        }

        if(notifications.isEmpty()){
            return null;
        }

        return notifications.get(0);

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
        BrokerListener listener = request.getListener();

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

                log.debug("Reconnect Event: "+host);

                resubscribe(host);
        }

    }


    private void resubscribe(HostInfo host){

        log.debug("Resubscribing : "+host);

        Map<String,BrokerAsyncConsumer> map =  consumerManager.removeSubscriptions(NetAction.DestinationType.QUEUE, host);

        for(Map.Entry<String, BrokerAsyncConsumer> entry : map.entrySet() ){
            BrokerAsyncConsumer consumer = entry.getValue();
            BrokerListener listener = entry.getValue().getListener();

            log.debug("Destination: "+entry.getKey());

            NetSubscribe subscribe = new NetSubscribe(consumer.getDestinationName(),consumer.getDestinationType());

            //@todo see Exception
            this.subscribeToHost(subscribe,listener,host);

        }


    }




}

