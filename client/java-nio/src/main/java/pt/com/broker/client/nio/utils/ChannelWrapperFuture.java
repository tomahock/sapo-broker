package pt.com.broker.client.nio.utils;

import io.netty.channel.ChannelFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import pt.com.broker.client.nio.server.HostInfo;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * @see LICENSE.TXT <p/>
 *      Created by Luis Santos<luis.santos@telecom.pt> on 23-06-2014.
 * @author vagrant
 * @version $Id: $Id
 */
public class ChannelWrapperFuture extends HostInfoFuture<HostInfo>
{

	protected ChannelFuture instance;

	/**
	 * <p>
	 * Constructor for ChannelWrapperFuture.
	 * </p>
	 *
	 * @param instance
	 *            a {@link io.netty.channel.ChannelFuture} object.
	 */
	public ChannelWrapperFuture(ChannelFuture instance)
	{
		this.instance = instance;
	}

	/**
	 * <p>
	 * Getter for the field <code>instance</code>.
	 * </p>
	 *
	 * @return a {@link io.netty.channel.ChannelFuture} object.
	 */
	public ChannelFuture getInstance()
	{
		return instance;
	}

	/** {@inheritDoc} */
	@Override
	public boolean cancel(boolean b)
	{
		return getInstance().cancel(b);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCancelled()
	{
		return getInstance().isCancelled();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDone()
	{
		return getInstance().isDone();
	}

	/** {@inheritDoc} */
	@Override
	public HostInfo get() throws InterruptedException, ExecutionException
	{

		getInstance().get();

		return getHostInfo();
	}

	/** {@inheritDoc} */
	@Override
	public HostInfo get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException
	{

		getInstance().get(l, timeUnit);

		return getHostInfo();
	}

	final private HostInfo getHostInfo()
	{

		ChannelDecorator channel = new ChannelDecorator(getInstance().channel());

		return channel.getHost();

	}
}
