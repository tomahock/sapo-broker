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

    protected final BindingSerializer serializer;


    public BaseChannelInitializer(BindingSerializer serializer) {
        this.serializer= serializer;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {


        if(isOldFraming()){

            /* add Message <> byte encode decoder */
            ch.pipeline().addLast("broker_message_decoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageDecoder(serializer));
            ch.pipeline().addLast("broker_message_encoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageEncoder(serializer));


        }else{

            /* add Message <> byte encode decoder */
            ch.pipeline().addLast("broker_message_decoder",new BrokerMessageDecoder(serializer));
            ch.pipeline().addLast("broker_message_encoder",new BrokerMessageEncoder(serializer));
        }




    }


    protected boolean isOldFraming(){
        return getProtocolType().equals(NetProtocolType.SOAP_v0);
    }

    private NetProtocolType getProtocolType() {
        return serializer.getProtocolType();
    }

}
