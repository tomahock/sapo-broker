package pt.com.broker.client.nio.bootstrap;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import org.caudexorigo.netty.NettyContext;

/**
 * Created by luissantos on 05-05-2014.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public class DatagramBootstrap extends BaseBootstrap
{

	/**
	 * <p>
	 * Constructor for DatagramBootstrap.
	 * </p>
	 *
	 * @param channelInitializer
	 *            a {@link pt.com.broker.client.nio.bootstrap.BaseChannelInitializer} object.
	 */
	public DatagramBootstrap(BaseChannelInitializer channelInitializer, NettyContext nettyCtx)
	{
		super(channelInitializer, nettyCtx);
	}

	/** {@inheritDoc} */
	@Override
	public Bootstrap getNewInstance(ByteBufAllocator allocator)
	{

		Bootstrap bootstrap = new Bootstrap();

		EventLoopGroup group = getGroup();

		bootstrap.group(group).channel(getNettyContext().getDatagramChannelClass()).option(ChannelOption.SO_BROADCAST, true);

		bootstrap.handler(getChannelInitializer());

		return bootstrap;
	}

}
