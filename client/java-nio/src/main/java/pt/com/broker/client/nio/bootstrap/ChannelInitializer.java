package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by luissantos on 06-05-2014.
 */
public class ChannelInitializer extends BaseChannelInitializer {


    protected ConsumerManager consumerManager;

    protected PongConsumerManager pongConsumerManager;

    protected SSLContext context;



    public ChannelInitializer(NetProtocolType protocolType, ConsumerManager consumerManager, PongConsumerManager pongConsumerManager) {

        super(protocolType);

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

        /* add message receive handler */
        ch.pipeline().addLast("broker_notification_handler",new ReceiveMessageHandler(getConsumerManager()));

        ch.pipeline().addLast("broker_pong_handler",new PongMessageHandler(getPongConsumerManager()));
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


    public SSLContext getContext() {
        return context;
    }

    public void setContext(SSLContext context) {
        this.context = context;
    }
}
