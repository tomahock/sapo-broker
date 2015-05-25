package pt.com.broker.client.nio.bootstrap;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.netty.NettyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.utils.ChannelDecorator;

/**
 *
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * @author vagrant
 * @version $Id: $Id
 */
public abstract class BaseBootstrap
{

	private static final Logger log = LoggerFactory.getLogger(BaseBootstrap.class);

	private final BaseChannelInitializer channelInitializer;

	private final EventLoopGroup group;

	private final ByteBufAllocator allocator;

	private NettyContext nettyCtx;

	/**
	 * <p>
	 * Constructor for BaseBootstrap.
	 * </p>
	 *
	 * @param channelInitializer
	 *            a {@link pt.com.broker.client.nio.bootstrap.BaseChannelInitializer} object.
	 */
	public BaseBootstrap(BaseChannelInitializer channelInitializer, NettyContext nettyCtx)
	{

		this.channelInitializer = channelInitializer;
		this.nettyCtx = nettyCtx;
		this.allocator = nettyCtx.getAllocator();
		this.group = nettyCtx.getBossEventLoopGroup();
	}

	/**
	 * <p>
	 * connect.
	 * </p>
	 *
	 * @param hostInfo
	 *            a {@link pt.com.broker.client.nio.server.HostInfo} object.
	 * @return a {@link io.netty.channel.ChannelFuture} object.
	 */
	public ChannelFuture connect(final HostInfo hostInfo)
	{
		io.netty.bootstrap.Bootstrap boot = getNewInstance(allocator);
		boot.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, hostInfo.getConnectTimeout());
		InetSocketAddress socketAddress = new InetSocketAddress(hostInfo.getHostname(), hostInfo.getPort());
		ChannelFuture f = boot.connect(socketAddress);
		f.addListener(new ChannelFutureListener()
		{

			@Override
			public void operationComplete(ChannelFuture f) throws Exception
			{
				if (f.isSuccess())
				{
					ChannelDecorator channel = new ChannelDecorator(f.channel());
					channel.setHost(hostInfo);
					hostInfo.setChannel(channel);
					hostInfo.setStatus(HostInfo.STATUS.OPEN);
					f.channel().closeFuture().addListener(new ChannelFutureListener()
					{

						@Override
						public void operationComplete(ChannelFuture future) throws Exception
						{
							hostInfo.setStatus(HostInfo.STATUS.CLOSED);
							hostInfo.setChannel(null);
						}

					});
				}
				else
				{
					hostInfo.setStatus(HostInfo.STATUS.CLOSED);
				}
			}

		});
		return f;
	}

	/**
	 * <p>
	 * Getter for the field <code>channelInitializer</code>.
	 * </p>
	 *
	 * @return a {@link pt.com.broker.client.nio.bootstrap.BaseChannelInitializer} object.
	 */
	public BaseChannelInitializer getChannelInitializer()
	{
		return channelInitializer;
	}

	/**
	 * <p>
	 * Getter for the field <code>group</code>.
	 * </p>
	 *
	 * @return a {@link io.netty.channel.EventLoopGroup} object.
	 */
	public EventLoopGroup getGroup()
	{
		return group;
	}

	public NettyContext getNettyContext()
	{
		return nettyCtx;
	}

	/**
	 * <p>
	 * getNewInstance.
	 * </p>
	 *
	 * @return a {@link io.netty.bootstrap.Bootstrap} object.
	 */

	public abstract io.netty.bootstrap.Bootstrap getNewInstance(ByteBufAllocator allocator);

	/**
	 * <p>
	 * shutdownGracefully.
	 * </p>
	 *
	 * @return a {@link io.netty.util.concurrent.Future} object.
	 */
	public Future<?> shutdownGracefully()
	{
		return group.shutdownGracefully();
	}

	/**
	 * <p>
	 * shutdownGracefully.
	 * </p>
	 *
	 * @param quietPeriod
	 *            a long.
	 * @param timeout
	 *            a long.
	 * @param unit
	 *            a {@link java.util.concurrent.TimeUnit} object.
	 * @return a {@link io.netty.util.concurrent.Future} object.
	 */
	public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit)
	{
		return group.shutdownGracefully(quietPeriod, timeout, unit);
	}
}
