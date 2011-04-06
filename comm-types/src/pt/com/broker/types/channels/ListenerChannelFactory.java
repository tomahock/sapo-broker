package pt.com.broker.types.channels;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;

public class ListenerChannelFactory
{
	private final static ConcurrentHashMap<Channel, ListenerChannel> channels = new ConcurrentHashMap<Channel, ListenerChannel>(); 
	
	public static ListenerChannel getListenerChannel(Channel channel)
	{
		ListenerChannel listenerChannel = new ListenerChannel(channel); //Inexpensive ctor.
		
		ListenerChannel previous = channels.putIfAbsent(channel, listenerChannel);
		
		return (previous == null) ? listenerChannel : previous;
	}
	
	public static void channelClosed(Channel channel)
	{
		channels.remove(channel);
	}	
}
