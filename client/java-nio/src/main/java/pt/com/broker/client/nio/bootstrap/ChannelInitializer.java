package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by luissantos on 05-05-2014.
 */
public class ChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {


    protected NetProtocolType protocolType;

    protected ConsumerManager consumerManager;

    protected PongConsumerManager pongConsumerManager;

    protected SSLContext context;

    public ChannelInitializer(NetProtocolType protocolType , ConsumerManager consumerManager , PongConsumerManager pongConsumerManager) {

        setProtocolType(protocolType);

        setConsumerManager(consumerManager);

        setPongConsumerManager(pongConsumerManager);
    }





    @Override
    protected void initChannel(SocketChannel ch) throws Exception {


        if(getContext()!=null){

            SSLEngine engine = getContext().createSSLEngine();

            engine.setUseClientMode(true);

            ch.pipeline().addFirst("ssl",
                    new SslHandler(engine, false));

        }


        if(isOldFraming()){

                    /* add Message <> byte encode decoder */
            ch.pipeline().addLast("broker_message_decoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageDecoder(getProtocolType()));
            ch.pipeline().addLast("broker_message_encoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageEncoder(getProtocolType()));

        }else{
                    /* add Message <> byte encode decoder */
            ch.pipeline().addLast("broker_message_decoder",new BrokerMessageDecoder(getProtocolType()));
            ch.pipeline().addLast("broker_message_encoder",new BrokerMessageEncoder(getProtocolType()));
        }



                /* add message receive handler */
        ch.pipeline().addLast("broker_notification_handler",new ReceiveMessageHandler(getConsumerManager()));

        ch.pipeline().addLast("broker_pong_handler",new PongMessageHandler(getPongConsumerManager()));

    }


    protected boolean isOldFraming(){
        return getProtocolType() == NetProtocolType.SOAP_v0;
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
