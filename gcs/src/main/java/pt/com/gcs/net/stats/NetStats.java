package pt.com.gcs.net.stats;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.MessageListener;
import pt.com.gcs.messaging.InboundRemoteChannels;
import pt.com.gcs.messaging.OutboundRemoteChannels;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;
import pt.com.gcs.messaging.TopicProcessor;
import pt.com.gcs.messaging.TopicProcessorList;

/**
 * Gathers the statistics about Queue and Topic subscriptions, both local and remote. It also tracks connections to other agents.
 * */
public class NetStats
{

	public static final String QUEUE_SUBSCRIPTIONS = "queue_connections";
	public static final String TOPIC_SUBSCRIPTIONS = "topic_connections";
	public static final String AGENT_CONNECTIONS = "agent_connections";

	private static final Logger log = LoggerFactory.getLogger(NetStats.class);

	private final String agentName;
	private final Map<String, List<SubscriptionInfo>> subscriptions;

	public NetStats(String agentName)
	{
		this.agentName = agentName;
		this.subscriptions = new HashMap<String, List<SubscriptionInfo>>();
	}

	private void collectStats()
	{
		collectQueueNetStats();
		collectTopicNetStats();
		collectAgentNetStats();
	}

	private void collectAgentNetStats()
	{
		List<SubscriptionInfo> agentConnections = new ArrayList<SubscriptionInfo>();
		SubscriptionInfo subscriptionInfo = new SubscriptionInfo(agentName);
		Map<String, ChannelHandlerContext> remoteConnections = InboundRemoteChannels.getAll();
		for (String agent : remoteConnections.keySet())
		{
			log.debug("Agent: {}", agent);
			subscriptionInfo.addRemoteListener((InetSocketAddress) remoteConnections.get(agent).channel().remoteAddress());
		}
		Map<String, Channel> localConnections = OutboundRemoteChannels.getAll();
		for (String agent : localConnections.keySet())
		{
			log.debug("Agent: {}", agent);
			subscriptionInfo.addLocalListener((InetSocketAddress) localConnections.get(agent).remoteAddress());
		}
		agentConnections.add(subscriptionInfo);
		subscriptions.put(AGENT_CONNECTIONS, agentConnections);
	}

	private void collectQueueNetStats()
	{
		List<SubscriptionInfo> queueSubscriptions = new ArrayList<SubscriptionInfo>();
		for (QueueProcessor p : QueueProcessorList.values())
		{
			SubscriptionInfo subscriptionInfo = new SubscriptionInfo(p.getSubscriptionName());
			Set<MessageListener> localListeners = p.localListeners();
			Set<MessageListener> remoteListeners = p.remoteListeners();
			addConnections(localListeners, subscriptionInfo, true);
			addConnections(remoteListeners, subscriptionInfo, false);
			queueSubscriptions.add(subscriptionInfo);
		}
		subscriptions.put(QUEUE_SUBSCRIPTIONS, queueSubscriptions);
	}

	private void collectTopicNetStats()
	{
		List<SubscriptionInfo> topicSubscriptions = new ArrayList<SubscriptionInfo>();
		for (TopicProcessor tp : TopicProcessorList.values())
		{
			SubscriptionInfo subscriptionInfo = new SubscriptionInfo(tp.getSubscriptionName());
			addConnections(tp.localListeners(), subscriptionInfo, true);
			addConnections(tp.remoteListeners(), subscriptionInfo, false);
			topicSubscriptions.add(subscriptionInfo);
		}
		subscriptions.put(TOPIC_SUBSCRIPTIONS, topicSubscriptions);
	}

	private void addConnections(Set<MessageListener> listeners, SubscriptionInfo subInfo, boolean localListener)
	{
		for (MessageListener listener : listeners)
		{
			if (localListener)
			{
				subInfo.addLocalListener((InetSocketAddress) listener.getChannel().getChannel().remoteAddress());
			}
			else
			{
				subInfo.addRemoteListener((InetSocketAddress) listener.getChannel().getChannel().remoteAddress());
			}
		}
	}

	public static final NetStats getStats(String agentName)
	{
		NetStats netStats = new NetStats(agentName);
		netStats.collectStats();
		return netStats;
	}

	public String getAgentName()
	{
		return agentName;
	}

	public Map<String, List<SubscriptionInfo>> getSubscriptions()
	{
		return subscriptions;
	}

}
