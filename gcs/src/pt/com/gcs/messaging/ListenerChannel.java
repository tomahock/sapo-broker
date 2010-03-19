package pt.com.gcs.messaging;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;

public class ListenerChannel
{
	private final Channel channel;

	public ListenerChannel(Channel channel)
	{
		super();
		this.channel = channel;
	}
	
	public ChannelFuture close()
	{
		return channel.close();
	}

	public ChannelFuture write(Object obj)
	{
		return channel.write(obj);
	}
	
	public boolean isConnected()
	{
		return channel.isConnected();
	}

	public boolean isWritable()
	{
		return channel.isWritable();
	}

	public boolean isReadable()
	{
		return channel.isReadable();
	}
	
	public Channel getChannel()
	{
		return channel;
	}

	public ChannelPipeline getPipeline()
	{
		return channel.getPipeline();
	}

	public String getRemoteAddressAsString()
	{
		return channel.getRemoteAddress().toString();
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
}
