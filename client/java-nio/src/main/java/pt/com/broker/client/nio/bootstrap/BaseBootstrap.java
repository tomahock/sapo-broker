package pt.com.broker.client.nio.bootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import pt.com.broker.client.nio.server.HostInfo;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 *
 *  Copyright (c) 2014, SAPO
 *  All rights reserved.
 *
 *
 */
public abstract class BaseBootstrap {

    private final BaseChannelInitializer channelInitializer;

    private final EventLoopGroup group = new NioEventLoopGroup();



    public BaseBootstrap(BaseChannelInitializer channelInitializer) {

        this.channelInitializer = channelInitializer;

    }


    public ChannelFuture connect(HostInfo hostInfo){

        io.netty.bootstrap.Bootstrap boot = getNewInstance();

        boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,hostInfo.getConnectTimeout());

        InetSocketAddress socketAddress = new InetSocketAddress(hostInfo.getHostname(),hostInfo.getPort());

        ChannelFuture f = boot.connect(socketAddress);

        return f;
    }


    public BaseChannelInitializer getChannelInitializer() {
        return channelInitializer;
    }

    public EventLoopGroup getGroup() {
        return group;
    }

    public abstract io.netty.bootstrap.Bootstrap getNewInstance();


    public Future<?> shutdownGracefully() {
        return group.shutdownGracefully();
    }

    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        return group.shutdownGracefully(quietPeriod, timeout, unit);
    }
}
