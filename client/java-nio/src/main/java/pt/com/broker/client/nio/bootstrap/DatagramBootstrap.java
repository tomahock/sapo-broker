package pt.com.broker.client.nio.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import pt.com.broker.client.nio.codecs.BindingSerializerFactory;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetProtocolType;

/**
 * Created by luissantos on 05-05-2014.
 */
public class DatagramBootstrap extends BaseBootstrap {


    public DatagramBootstrap(BaseChannelInitializer channelInitializer) {
        super(channelInitializer);
    }

    @Override
    public Bootstrap getNewInstance() {

        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup group = getGroup();

        bootstrap.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true);

        bootstrap.handler(getChannelInitializer());

        return bootstrap;
    }


}
