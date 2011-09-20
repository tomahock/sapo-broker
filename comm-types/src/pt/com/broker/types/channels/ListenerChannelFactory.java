package pt.com.broker.types.channels;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

public class ListenerChannelFactory
{
	private final static ConcurrentHashMap<Channel, ListenerChannel> channels = new ConcurrentHashMap<Channel, ListenerChannel>();

	public static ListenerChannel getListenerChannel(ChannelHandlerContext context)
	{
		Channel channel = context.getChannel();

		ListenerChannel listenerChannel = new ListenerChannel(context); // Inexpensive ctor.

		ListenerChannel previous = channels.putIfAbsent(channel, listenerChannel);

		return (previous == null) ? listenerChannel : previous;
	}

	public static void channelClosed(Channel channel)
	{
		channels.remove(channel);
	}
}
