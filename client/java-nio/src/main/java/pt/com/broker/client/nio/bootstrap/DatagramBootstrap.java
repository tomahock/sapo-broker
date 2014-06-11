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
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class DatagramBootstrap extends BaseBootstrap {


    /**
     * <p>Constructor for DatagramBootstrap.</p>
     *
     * @param channelInitializer a {@link pt.com.broker.client.nio.bootstrap.BaseChannelInitializer} object.
     */
    public DatagramBootstrap(BaseChannelInitializer channelInitializer) {
        super(channelInitializer);
    }

    /** {@inheritDoc} */
    @Override
    public Bootstrap getNewInstance() {

        Bootstrap bootstrap = new Bootstrap();

        EventLoopGroup group = getGroup();

        bootstrap.group(group).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true);

        bootstrap.handler(getChannelInitializer());

        return bootstrap;
    }


}
