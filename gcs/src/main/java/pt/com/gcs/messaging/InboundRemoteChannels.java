package pt.com.gcs.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InboundRemoteChannels
{
	private static Logger log = LoggerFactory.getLogger(InboundRemoteChannels.class);

	private static ConcurrentHashMap<String, ChannelHandlerContext> remoteChannels = new ConcurrentHashMap<String, ChannelHandlerContext>();

	/*
	 * returns the channel previously associated with agentId, or null if any.
	 */
	public static ChannelHandlerContext add(String agentId, ChannelHandlerContext channel)
	{
		ChannelHandlerContext previous = remoteChannels.put(agentId, channel);
		log.info("Adding new ChannelHandlerContext to InboundRemoteChannels. Current size: " + remoteChannels.size());
		return previous;
	}

	public static ChannelHandlerContext get(String agentId)
	{
		return remoteChannels.get(agentId);
	}

	/*
	 * returns the channel associated with agentId, or null if any.
	 */
	public static boolean remove(ChannelHandlerContext channel)
	{
		for (Entry<String, ChannelHandlerContext> entry : remoteChannels.entrySet())
		{
			if (entry.getValue().equals(channel))
			{
				remoteChannels.remove(entry.getKey());
				return true;
			}
		}
		return false;
	}

	public static Map<String, ChannelHandlerContext> getAll()
	{
		return new HashMap<String, ChannelHandlerContext>(remoteChannels);
	}
}
