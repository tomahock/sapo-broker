package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by luissantos on 05-05-2014.
 */
public abstract class BaseChannelInitializer extends io.netty.channel.ChannelInitializer<Channel> {


    protected static final Logger log = LoggerFactory.getLogger(BaseChannelInitializer.class);


    protected final NetProtocolType protocolType;


    public BaseChannelInitializer(NetProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {



        BindingSerializer binding = BindingSerializerFactory.getInstance(getProtocolType());


        if(isOldFraming()){

            /* add Message <> byte encode decoder */
            ch.pipeline().addLast("broker_message_decoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageDecoder(binding));
            ch.pipeline().addLast("broker_message_encoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageEncoder(binding));


        }else{

            /* add Message <> byte encode decoder */
            ch.pipeline().addLast("broker_message_decoder",new BrokerMessageDecoder(binding));
            ch.pipeline().addLast("broker_message_encoder",new BrokerMessageEncoder(binding));
        }




    }


    protected boolean isOldFraming(){
        return getProtocolType() == NetProtocolType.SOAP_v0;
    }

    public NetProtocolType getProtocolType() {
        return protocolType;
    }

}
