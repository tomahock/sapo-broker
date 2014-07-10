package pt.com.broker.types.channels;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

import java.util.concurrent.atomic.AtomicLong;



public class ListenerChannel
{
	public enum ChannelState
	{
		NOT_READY, READY
	};

	public static final long MAX_WRITE_TRIES = 100;

	private final Channel channel;
	private final AtomicLong messageDeliveryTries = new AtomicLong(0);

	private final ChannelHandlerContext context;

	protected ListenerChannel(ChannelHandlerContext context)
	{
		super();
		this.context = context;
		this.channel = context.channel();
	}

	public ChannelFuture close()
	{
		return channel.close();
	}

	public ChannelFuture write(Object obj)
	{
		return channel.write(obj);
	}
    public ChannelFuture writeAndFlush(Object obj)
    {
        return channel.writeAndFlush(obj);
    }

	public boolean isConnected()
	{
		return channel.isActive();
	}

	public boolean isWritable()
	{
		return channel.isWritable();
	}



	public Channel getChannel()
	{
		return channel;
	}

	public ChannelHandlerContext getChannelContext()
	{
		return context;
	}

	public ChannelPipeline getPipeline()
	{
		return channel.pipeline();
	}

	public String getRemoteAddressAsString()
	{
		return channel.remoteAddress().toString();
	}

	@Override
	public String toString()
	{
		return "ListenerChannel [channel=" + getRemoteAddressAsString() + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : getRemoteAddressAsString().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ListenerChannel other = (ListenerChannel) obj;
		if (channel == null)
		{
			if (other.channel != null)
				return false;
		}
		else if (!getRemoteAddressAsString().equals(other.getRemoteAddressAsString()))
			return false;
		return true;
	}

	public long incrementAndGetDeliveryTries()
	{
		return messageDeliveryTries.incrementAndGet();
	}

	public long decrementAndGetDeliveryTries()
	{
		return messageDeliveryTries.decrementAndGet();
	}

	public long getDeliveryTries()
	{
		return messageDeliveryTries.get();
	}

	public void resetDeliveryTries()
	{
		messageDeliveryTries.set(0);
	}
}