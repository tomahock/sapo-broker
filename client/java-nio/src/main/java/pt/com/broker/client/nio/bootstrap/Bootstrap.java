package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import pt.com.broker.client.nio.HostInfo;
import pt.com.broker.client.nio.NioSocketChannelBroker;
import pt.com.broker.client.nio.codecs.BrokerMessageDecoder;
import pt.com.broker.client.nio.codecs.BrokerMessageEncoder;
import pt.com.broker.client.nio.consumer.ConsumerManager;
import pt.com.broker.client.nio.handlers.ReceiveMessageHandler;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 23-04-2014.
 */
public class Bootstrap {

    io.netty.bootstrap.Bootstrap bootstrap;

    private NetProtocolType protocolType;

    ConsumerManager consumerManager;

    public Bootstrap(NetProtocolType protocolType , ConsumerManager consumerManager) {

        setProtocolType(protocolType);

        setBootstrap(new io.netty.bootstrap.Bootstrap());

        init();
    }

    public void init(){


        EventLoopGroup group = new NioEventLoopGroup();



        getBootstrap().group(group).channel(NioSocketChannelBroker.class);

        getBootstrap().handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {

                            /* add Message <> byte encode decoder */
                ch.pipeline().addLast(new BrokerMessageDecoder(getProtocolType()));
                ch.pipeline().addLast(new BrokerMessageEncoder(getProtocolType()));

                            /* add message receive handler */
                ch.pipeline().addLast(new ReceiveMessageHandler(getConsumerManager()));

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


    public ChannelFuture connect(HostInfo hostInfo){
        ChannelFuture f = getBootstrap().connect(hostInfo.getSocketAddress());

        hostInfo.setChannelFuture(f);

        return f;
    }
}
