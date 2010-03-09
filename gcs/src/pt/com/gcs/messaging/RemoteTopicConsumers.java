package pt.com.gcs.messaging;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.netty.channel.Channel;
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

	private static final RemoteTopicConsumers instance = new RemoteTopicConsumers();

	private Map<String, CopyOnWriteArrayList<Channel>> remoteTopicConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<Channel>>();

	private static final int WRITE_BUFFER_SIZE = 128 * 1024;

	private RemoteTopicConsumers()
	{
	}

	protected synchronized static void add(String topicName, Channel channel)
	{
		log.info("Adding new remote topic consumer for topic:  '{}'", topicName);
		try
		{
			CopyOnWriteArrayList<Channel> sessions = instance.remoteTopicConsumers.get(topicName);
			if (sessions == null)
			{
				sessions = new CopyOnWriteArrayList<Channel>();
			}

			if (!sessions.contains(channel))
			{
				sessions.add(channel);
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
				CopyOnWriteArrayList<Channel> sessions = instance.remoteTopicConsumers.get(topicName);
				if (sessions != null)
				{
					sessions.remove(channel);
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
			CopyOnWriteArrayList<Channel> sessions = instance.remoteTopicConsumers.get(topicName);
			if (sessions != null)
			{
				sessions.remove(channel);
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
		CopyOnWriteArrayList<Channel> sessions = instance.remoteTopicConsumers.get(destinationName);
		if (sessions != null)
		{
			return sessions.size();
		}
		return 0;
	}

	private void doNotify(String subscriptionName, InternalMessage message)
	{
		try
		{
			CopyOnWriteArrayList<Channel> sessions = remoteTopicConsumers.get(subscriptionName);
			if (sessions != null)
			{
				if (sessions.size() == 0)
				{
					log.debug("There are no remote peers to deliver the message.");
					return;
				}

				log.debug("There are {} remote peer(s) to deliver the message.", sessions.size());

				for (Channel channel : sessions)
				{
					if (channel != null)
					{
						if (channel.isOpen() && channel.isWritable())
						{
							channel.write(message);
						}
						else
						{
							// Discard message
							String log_msg = String.format("Write Queue is full, discard message. MessageId: '%s', Destination: '%s', Target Agent: '%s'", message.getMessageId(), message.getDestination(), channel.getRemoteAddress().toString());
							log.warn(log_msg);

							String dname = String.format("/system/warn/write-queue/#%s#", GcsInfo.getAgentName());
							String info_msg = String.format("%s#%s#%s", message.getMessageId(), message.getDestination(), channel.getRemoteAddress().toString());
							InternalPublisher.send(dname, info_msg);
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
	
	public synchronized static CopyOnWriteArrayList<Channel> getSubscription(String subscriptionName)
	{
		 return instance.remoteTopicConsumers.get(subscriptionName);
	}
	
	
	public synchronized static Set<String> getSubscriptionNames()
	{
		 return instance.remoteTopicConsumers.keySet();
	}
}
