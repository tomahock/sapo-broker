package pt.com.gcs.messaging;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GcsInfo;

/**
 * RemoteTopicConsumers maintains current remote topic consumers (other agents).
 * 
 */
public class RemoteTopicConsumers
{
	private static Logger log = LoggerFactory.getLogger(RemoteTopicConsumers.class);

	private static final long MAX_WRITE_TIME = 50;

	private static final RemoteTopicConsumers instance = new RemoteTopicConsumers();

	private Map<String, CopyOnWriteArrayList<ChannelInfo>> remoteTopicConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<ChannelInfo>>();

	public static class ChannelInfo
	{
		public Channel channel;
		public AtomicBoolean isDiscarding;
		public AtomicLong deliveryTime;

		public ChannelInfo(Channel channel)
		{
			this.channel = channel;
			isDiscarding = new AtomicBoolean(false);
			deliveryTime = new AtomicLong(0);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((channel == null) ? 0 : channel.hashCode());
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
			ChannelInfo other = (ChannelInfo) obj;
			if (channel == null)
			{
				if (other.channel != null)
					return false;
			}
			else if (!channel.equals(other.channel))
				return false;
			return true;
		}

		public boolean isReady()
		{
			return isReady(System.nanoTime());
		}

		public boolean isReady(long currentTime)
		{
			return currentTime >= deliveryTime.get();
		}

	}

	private RemoteTopicConsumers()
	{
	}

	protected synchronized static void add(String topicName, Channel channel)
	{
		log.info("Adding new remote topic consumer for topic:  '{}'", topicName);
		try
		{
			CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteTopicConsumers.get(topicName);
			if (sessions == null)
			{
				sessions = new CopyOnWriteArrayList<ChannelInfo>();
			}

			if (!sessions.contains(channel))
			{
				sessions.add(new ChannelInfo(channel));
				log.info("Add remote topic consumer for '{}'", topicName);
			}
			else
			{
				log.info("Remote topic consumer for '{}' and session '{}' already exists", topicName, channel.getRemoteAddress().toString());
			}

			instance.remoteTopicConsumers.put(topicName, sessions);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
	}

	protected static void notify(InternalMessage message)
	{
		if (instance.remoteTopicConsumers.size() > 0)
		{
			String topicName = message.getDestination();
			Set<String> subscriptionNames = instance.remoteTopicConsumers.keySet();

			Set<String> matches = new HashSet<String>();
			for (String sname : subscriptionNames)
			{
				if (sname.equals(topicName))
				{
					matches.add(topicName);
				}
				else
				{
					if (DestinationMatcher.match(sname, topicName))
						matches.add(sname);
				}
			}

			for (String subscriptionName : matches)
			{
				instance.doNotify(subscriptionName, message);
			}
		}
	}

	protected synchronized static void remove(Channel channel)
	{
		try
		{
			Set<String> keys = instance.remoteTopicConsumers.keySet();
			for (String topicName : keys)
			{
				CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteTopicConsumers.get(topicName);
				if (sessions != null)
				{
					sessions.remove(new ChannelInfo(channel));
					log.info("Remove remote topic consumer for '{}' and session '{}'", topicName, channel.getRemoteAddress().toString());
				}
				instance.remoteTopicConsumers.put(topicName, sessions);
			}
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
	}

	protected synchronized static void remove(String topicName, Channel channel)
	{
		try
		{
			CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteTopicConsumers.get(topicName);
			if (sessions != null)
			{
				sessions.remove(new ChannelInfo(channel));
			}
			instance.remoteTopicConsumers.put(topicName, sessions);
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
	}

	protected synchronized static int size()
	{
		return instance.remoteTopicConsumers.size();
	}

	protected synchronized static int size(String destinationName)
	{
		CopyOnWriteArrayList<ChannelInfo> sessions = instance.remoteTopicConsumers.get(destinationName);
		if (sessions != null)
		{
			return sessions.size();
		}
		return 0;
	}

	AtomicInteger droppedMessages = new AtomicInteger(0);

	private void doNotify(String subscriptionName, InternalMessage message)
	{
		try
		{
			CopyOnWriteArrayList<ChannelInfo> sessions = remoteTopicConsumers.get(subscriptionName);
			if (sessions != null)
			{
				if (sessions.size() == 0)
				{
					log.debug("There are no remote peers to deliver the message.");
					return;
				}

				log.debug("There are {} remote peer(s) to deliver the message.", sessions.size());

				for (final ChannelInfo channelInfo : sessions)
				{
					Channel channel = channelInfo.channel;
					if (channel.isWritable())
					{
						channel.write(message);
						if (channelInfo.isDiscarding.compareAndSet(true, false))
						{
							String msg = String.format("Stopped discarding messages for topic '%s' and session '%s'. Dropped messages: %s", subscriptionName, channelInfo.channel.getRemoteAddress().toString(), droppedMessages.getAndSet(0));
							log.info(msg);
						}
					}
					else
					{
						if (channelInfo.isReady())
						{
							ChannelFuture future = channel.write(message);
							channelInfo.deliveryTime.set(System.nanoTime() + (10 * 1000));
							final long writeStartTime = System.nanoTime();

							future.addListener(new ChannelFutureListener()
							{
								@Override
								public void operationComplete(ChannelFuture future) throws Exception
								{
									final long writeCompleteTime = ((System.nanoTime() - writeStartTime));

									long delayTime = 0;
									if (writeCompleteTime >= MAX_WRITE_TIME)
									{
										delayTime = System.nanoTime() + (writeCompleteTime / 2);
										channelInfo.deliveryTime.set(delayTime);
									}
								}
							});
						}
						else
						{
							if (!channelInfo.isDiscarding.getAndSet(true))
							{
								log.info("Started discarding messages for topic '{}' and session '{}'.", subscriptionName, channelInfo.channel.getRemoteAddress().toString());

								String dname = String.format("/system/warn/write-queue/#%s#", GcsInfo.getAgentName());
								String info_msg = String.format("%s#%s#%s", message.getMessageId(), message.getDestination(), channel.getRemoteAddress().toString());
								InternalPublisher.send(dname, info_msg);
							}
							droppedMessages.incrementAndGet();
						}
					}
				}
			}
			else
			{
				log.info("There are no remote consumers for topic: '{}'", message.getDestination());
			}
		}
		catch (Throwable t)
		{
			log.error(t.getMessage());
		}
	}

	public synchronized static CopyOnWriteArrayList<ChannelInfo> getSubscription(String subscriptionName)
	{
		return instance.remoteTopicConsumers.get(subscriptionName);
	}

	public synchronized static Set<String> getSubscriptionNames()
	{
		return instance.remoteTopicConsumers.keySet();
	}
}
