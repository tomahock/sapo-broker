package pt.com.broker.client.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;

/**
 * Created by luissantos on 21-04-2014.
 */
public class BrokerClient {

    private final String host;
    private final int port;

    private Bootstrap bootstrap;

    public BrokerClient(String host, int port) {

        this.host = host;
        this.port = port;

        bootstrap = new Bootstrap();
    }

    public ChannelFuture connect(){

        EventLoopGroup group = new NioEventLoopGroup();


         bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(new InitChannelHandler());

                        }
                    });

        ChannelFuture f = bootstrap.connect();

        return f;

    }

    public Future close(){
        return getBootstrap().group().shutdownGracefully();
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }
}
