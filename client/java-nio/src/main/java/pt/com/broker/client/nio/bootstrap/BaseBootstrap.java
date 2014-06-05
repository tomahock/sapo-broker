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
 *  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the SAPO nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
