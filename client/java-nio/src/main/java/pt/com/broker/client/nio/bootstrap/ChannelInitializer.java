package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.Channel;
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

/**
 * Created by luissantos on 06-05-2014.
 */
public class ChannelInitializer extends BaseChannelInitializer {


    protected ConsumerManager consumerManager;

    protected PongConsumerManager pongConsumerManager;

    protected PendingAcceptRequestsManager acceptRequestsManager;

    protected SSLContext context;

    protected ReceiveFaultHandler faultHandler;


    public ChannelInitializer(BindingSerializer serializer, ConsumerManager consumerManager, PongConsumerManager pongConsumerManager) {

        super(serializer);

        setConsumerManager(consumerManager);

        setPongConsumerManager(pongConsumerManager);

    }

    @Override
    protected void initChannel(Channel ch) throws Exception {

        super.initChannel(ch);

        if(getContext()!=null){

            SSLEngine engine = getContext().createSSLEngine();

            engine.setUseClientMode(true);

            ch.pipeline().addFirst("ssl", new SslHandler(engine, false) );

        }

        ch.pipeline().addLast("idle_state_handler", new IdleStateHandler(4, 2, 0));
        ch.pipeline().addLast("heartbeat_handler", new HeartbeatHandler());


        /* add message receive handler */
        ch.pipeline().addLast("broker_notification_handler",new ReceiveMessageHandler(getConsumerManager()));

        ch.pipeline().addLast("broker_pong_handler",new PongMessageHandler(getPongConsumerManager()));

        faultHandler = new ReceiveFaultHandler(getConsumerManager());

        ch.pipeline().addLast("broker_fault_handler",faultHandler);

        if(getAcceptRequestsManager()!=null){
            ch.pipeline().addLast("broker_accept_handler",new AcceptMessageHandler(getAcceptRequestsManager()));
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

    public PendingAcceptRequestsManager getAcceptRequestsManager() {
        return acceptRequestsManager;
    }

    public void setAcceptRequestsManager(PendingAcceptRequestsManager acceptRequestsManager) {
        this.acceptRequestsManager = acceptRequestsManager;
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
