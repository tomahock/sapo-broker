package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import io.netty.handler.ssl.SslHandler;

import pt.com.broker.client.nio.codecs.HeartbeatHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.handlers.AcceptMessageHandler;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveFaultHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.BindingSerializer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;


/**
 * Created by luissantos on 06-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class ChannelInitializer extends BaseChannelInitializer {

    
    private final HeartbeatHandler heartbeatHandler = new HeartbeatHandler();
    private final PongMessageHandler pongMessageHandler;
    private final ReceiveFaultHandler faultHandler;
    private final AcceptMessageHandler acceptMessageHandler;
    private final ReceiveMessageHandler receiveMessageHandler;


    protected ConsumerManager consumerManager;
    protected PongConsumerManager pongConsumerManager;
    protected PendingAcceptRequestsManager acceptRequestsManager;

    protected SSLContext context;




    /**
     * <p>Constructor for ChannelInitializer.</p>
     *
     * @param serializer a {@link pt.com.broker.types.BindingSerializer} object.
     * @param consumerManager a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
     * @param pongConsumerManager a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
     */
    public ChannelInitializer(BindingSerializer serializer, ConsumerManager consumerManager, PongConsumerManager pongConsumerManager) {

        super(serializer);

        setConsumerManager(consumerManager);

        setPongConsumerManager(pongConsumerManager);

        pongMessageHandler =  new PongMessageHandler(getPongConsumerManager());

        faultHandler = new ReceiveFaultHandler(getConsumerManager());

        acceptMessageHandler = new AcceptMessageHandler(null);

        receiveMessageHandler = new ReceiveMessageHandler(getConsumerManager());

    }

    /** {@inheritDoc} */
    @Override
    protected void initChannel(Channel ch) throws Exception {

        super.initChannel(ch);

        ChannelPipeline pipeline = ch.pipeline();



        SSLContext sslContext = getContext();

        if( sslContext !=null ){

            SSLEngine engine = sslContext.createSSLEngine();

            engine.setUseClientMode(true);

            pipeline.addFirst("ssl", new SslHandler(engine, false) );

        }

        pipeline.addLast("heartbeat_handler", heartbeatHandler);


        /* add message receive handler */
        pipeline.addLast("broker_notification_handler",receiveMessageHandler);

        pipeline.addLast("broker_pong_handler",pongMessageHandler);
        pipeline.addLast("broker_fault_handler", faultHandler);
        pipeline.addLast("broker_accept_handler",acceptMessageHandler);

    }


    /**
     * <p>Getter for the field <code>consumerManager</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
     */
    public ConsumerManager getConsumerManager() {
        return consumerManager;
    }

    /**
     * <p>Setter for the field <code>consumerManager</code>.</p>
     *
     * @param consumerManager a {@link pt.com.broker.client.nio.consumer.ConsumerManager} object.
     */
    public void setConsumerManager(ConsumerManager consumerManager) {
        this.consumerManager = consumerManager;
    }

    /**
     * <p>Getter for the field <code>pongConsumerManager</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
     */
    public PongConsumerManager getPongConsumerManager() {
        return pongConsumerManager;
    }

    /**
     * <p>Setter for the field <code>pongConsumerManager</code>.</p>
     *
     * @param pongConsumerManager a {@link pt.com.broker.client.nio.consumer.PongConsumerManager} object.
     */
    public void setPongConsumerManager(PongConsumerManager pongConsumerManager) {

        this.pongConsumerManager = pongConsumerManager;

        if(pongMessageHandler!=null){
            pongMessageHandler.setManager(pongConsumerManager);
        }

    }

    /**
     * <p>Getter for the field <code>acceptRequestsManager</code>.</p>
     *
     * @return a {@link pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager} object.
     */
    public PendingAcceptRequestsManager getAcceptRequestsManager() {
        return acceptRequestsManager;
    }

    /**
     * <p>Setter for the field <code>acceptRequestsManager</code>.</p>
     *
     * @param acceptRequestsManager a {@link pt.com.broker.client.nio.consumer.PendingAcceptRequestsManager} object.
     */
    public void setAcceptRequestsManager(PendingAcceptRequestsManager acceptRequestsManager) {
        this.acceptRequestsManager = acceptRequestsManager;
        acceptMessageHandler.setManager(acceptRequestsManager);
    }

    /**
     * <p>Getter for the field <code>context</code>.</p>
     *
     * @return a {@link javax.net.ssl.SSLContext} object.
     */
    public SSLContext getContext() {
        return context;
    }

    /**
     * <p>Setter for the field <code>context</code>.</p>
     *
     * @param context a {@link javax.net.ssl.SSLContext} object.
     */
    public void setContext(SSLContext context) {
        this.context = context;
    }


    /**
     * <p>Setter for the field <code>faultHandler</code>.</p>
     *
     * @param adapter a {@link pt.com.broker.client.nio.events.BrokerListener} object.
     */
    public void setFaultHandler(BrokerListener adapter){

        faultHandler.setFaultListenerAdapter(adapter);

    }




}
