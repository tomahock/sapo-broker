package pt.com.broker.client.nio.bootstrap;

import io.netty.bootstrap.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import pt.com.broker.client.nio.NioSocketChannelBroker;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.NetProtocolType;

import java.nio.channels.DatagramChannel;

/**
 * Created by luissantos on 05-05-2014.
 */
public class DatagramBootstrap extends BaseBootstrap {




    public DatagramBootstrap(NetProtocolType protocolType, boolean oldFraming) {
        this.setProtocolType(protocolType);


        setBootstrap(new Bootstrap());

        setOldFraming(oldFraming);

        init();
    }

    public void init(){


        EventLoopGroup group = new NioEventLoopGroup();



        getBootstrap().group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true);


        getBootstrap().handler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {

                if(isOldFraming()){

                    /* add Message <> byte encode decoder */
                    ch.pipeline().addLast("broker_message_decoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageDecoder(getProtocolType()));
                    ch.pipeline().addLast("broker_message_encoder",new pt.com.broker.client.nio.codecs.oldframing.BrokerMessageEncoder(getProtocolType()));

                }else{
                    /* add Message <> byte encode decoder */
                    ch.pipeline().addLast("broker_message_decoder",new BrokerMessageDecoder(getProtocolType()));
                    ch.pipeline().addLast("broker_message_encoder",new BrokerMessageEncoder(getProtocolType()));
                }
            }


        });

    }



}
