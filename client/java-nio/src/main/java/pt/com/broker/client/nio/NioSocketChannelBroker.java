package pt.com.broker.client.nio;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
/**
 * Created by luissantos on 23-04-2014.
 */
public class NioSocketChannelBroker extends io.netty.channel.socket.nio.NioSocketChannel{


    @Override
    public ChannelPromise newPromise() {
        return super.newPromise();
    }
}
