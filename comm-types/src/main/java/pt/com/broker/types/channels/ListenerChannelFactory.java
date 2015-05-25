package pt.com.broker.types.channels;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;

public class ListenerChannelFactory
{
	private final static ConcurrentHashMap<Channel, ListenerChannel> channels = new ConcurrentHashMap<Channel, ListenerChannel>();

	public static ListenerChannel getListenerChannel(ChannelHandlerContext context)
	{
		Channel channel = context.channel();

		ListenerChannel listenerChannel = new ListenerChannel(context); // Inexpensive ctor.

		ListenerChannel previous = channels.putIfAbsent(channel, listenerChannel);

		return (previous == null) ? listenerChannel : previous;
	}

	public static void channelClosed(Channel channel)
	{
		channels.remove(channel);
	}
}