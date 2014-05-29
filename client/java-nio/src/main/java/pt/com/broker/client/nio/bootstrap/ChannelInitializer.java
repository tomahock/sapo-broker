package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
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
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.util.concurrent.TimeUnit;

/**
 * Created by luissantos on 06-05-2014.
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




    public ChannelInitializer(BindingSerializer serializer, ConsumerManager consumerManager, PongConsumerManager pongConsumerManager) {

        super(serializer);

        setConsumerManager(consumerManager);

        setPongConsumerManager(pongConsumerManager);

        pongMessageHandler =  new PongMessageHandler(getPongConsumerManager());

        faultHandler = new ReceiveFaultHandler(getConsumerManager());

        acceptMessageHandler = new AcceptMessageHandler(null);

        receiveMessageHandler = new ReceiveMessageHandler(getConsumerManager());

    }

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

        IdleStateHandler idleStateHandler = new IdleStateHandler(4000, 2000, 0, TimeUnit.MILLISECONDS);

        pipeline.addLast("idle_state_handler", idleStateHandler );
        pipeline.addLast("heartbeat_handler", heartbeatHandler);

        /* add message receive handler */
        pipeline.addLast("broker_notification_handler",receiveMessageHandler);

        pipeline.addLast("broker_pong_handler",pongMessageHandler);
        pipeline.addLast("broker_fault_handler", faultHandler);
        pipeline.addLast("broker_accept_handler",acceptMessageHandler);

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

        if(pongMessageHandler!=null){
            pongMessageHandler.setManager(pongConsumerManager);
        }

    }

    public PendingAcceptRequestsManager getAcceptRequestsManager() {
        return acceptRequestsManager;
    }

    public void setAcceptRequestsManager(PendingAcceptRequestsManager acceptRequestsManager) {
        this.acceptRequestsManager = acceptRequestsManager;
        acceptMessageHandler.setManager(acceptRequestsManager);
    }

    public SSLContext getContext() {
        return context;
    }

    public void setContext(SSLContext context) {
        this.context = context;
    }


    public void setFaultHandler(BrokerListener adapter){

        faultHandler.setFaultListenerAdapter(adapter);

    }
}
