package pt.com.broker.client.nio.bootstrap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.NioSocketChannelBroker;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.consumer.PongConsumerManager;
import pt.com.broker.client.nio.handlers.PongMessageHandler;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 23-04-2014.
 */
public class Bootstrap extends BaseBootstrap {



    private NetProtocolType protocolType;

    ConsumerManager consumerManager;

    PongConsumerManager pongConsumerManager;

    public Bootstrap(NetProtocolType protocolType , ConsumerManager consumerManager, PongConsumerManager pongConsumerManager, boolean oldFraming) {

        setProtocolType(protocolType);

        setBootstrap(new io.netty.bootstrap.Bootstrap());

        setPongConsumerManager(pongConsumerManager);
        setConsumerManager(consumerManager);

        init(oldFraming);
    }

    public void init(final boolean oldFraming){


        EventLoopGroup group = new NioEventLoopGroup();



        getBootstrap().group(group).channel(NioSocketChannelBroker.class);

        getBootstrap().handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {


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
        });

    }



    public io.netty.bootstrap.Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(io.netty.bootstrap.Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
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


}
