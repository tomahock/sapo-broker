package pt.com.gcs.messaging;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundRemoteChannels
{
	private static Logger log = LoggerFactory.getLogger(OutboundRemoteChannels.class);

	private static ConcurrentHashMap<String, Channel> remoteChannels = new ConcurrentHashMap<String, Channel>();

	/*
	 * returns the channel previously associated with agentId, or null if any.
	 */
	public static Channel add(String agentId, Channel channel)
	{

		Channel previous = remoteChannels.put(agentId, channel);
		log.info("Adding new Channel to OutboundRemoteChannels. Current size: " + remoteChannels.size());
		return previous;
	}

	public static boolean contains(String agentId)
	{
		return remoteChannels.contains(agentId);
	}

	public static Channel get(String agentId)
	{
		return remoteChannels.get(agentId);
	}

	public static void remove(String agentId)
	{
		remoteChannels.remove(agentId);
	}

	public static boolean remove(Channel channel)
	{
		boolean remove = remoteChannels.remove(socketToAgentId(channel.getRemoteAddress()), channel);

		return remove;
	}

	public static Map<String, Channel> getAll()
	{
		return new HashMap<String, Channel>(remoteChannels);
	}

	public static String socketToAgentId(SocketAddress socketAddress)
	{
		InetSocketAddress remoteAddress = (InetSocketAddress) socketAddress;
		byte[] ip = remoteAddress.getAddress().getAddress();
		int port = remoteAddress.getPort();

		return String.format("%s.%s.%s.%s:%s", (int) ip[0] & 0xFF, (int) ip[1] & 0xFF, (int) ip[2] & 0xFF, (int) ip[3] & 0xFF, port);
	}
}
