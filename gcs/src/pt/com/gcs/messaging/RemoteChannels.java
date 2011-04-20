package pt.com.gcs.messaging;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;

public class RemoteChannels
{
	private static ConcurrentHashMap<String, Channel> remoteChannels = new ConcurrentHashMap<String, Channel>();

	/*
	 * returns the channel previously associated with agentId, or null if any.
	 */
	public static Channel add(String agentId, Channel channel)
	{
		return remoteChannels.put(agentId, channel);
	}
	
	public static Channel get(String agentId)
	{
		return remoteChannels.get(agentId);
	}
	
	/*
	 * returns the channel associated with agentId, or null if any.
	 */
	public static boolean remove(Channel channel)
	{
		for(Entry<String,Channel> entry : remoteChannels.entrySet())
		{
			if(entry.getValue().equals(channel))
			{
				remoteChannels.remove(entry.getKey());
				return true;
			}
		}		
		return false;
	}
}
