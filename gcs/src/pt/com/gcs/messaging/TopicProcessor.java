package pt.com.gcs.messaging;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.MessageListener;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.gcs.conf.GcsInfo;

public class TopicProcessor
{
	private static Logger log = LoggerFactory.getLogger(TopicProcessor.class);

	private final String subscriptionName;

	private final Set<MessageListener> topicListeners = new CopyOnWriteArraySet<MessageListener>();

	private boolean broadcastable;

	protected TopicProcessor(String subscriptionName)
	{
		if (StringUtils.isBlank(subscriptionName))
		{
			throw new IllegalArgumentException("Subscription names can not be blank");
		}
		this.subscriptionName = subscriptionName;
		try
		{
			Pattern.compile(subscriptionName);
		}
		catch (Throwable e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	public void add(MessageListener listener, boolean broadcast)
	{
		if (listener != null)
		{
			synchronized (topicListeners)
			{

				if (listener.getType() == MessageListener.Type.REMOTE)
				{
					addRemote(listener);
				}
				else
				{
					addLocal(listener);
				}
			}
		}
		else
		{
			throw new IllegalArgumentException(String.format("Cannot add null listener to subscription '%s'", subscriptionName));
		}
	}

	private void addLocal(MessageListener listener)
	{
		if (topicListeners.add(listener))
		{
			log.info("Add listener -> '{}'", listener.toString());

			if (listener.getType() == MessageListener.Type.LOCAL)
			{
				broadCastNewTopicConsumer();
				broadcastable = true;
			}
		}
	}

	private void addRemote(MessageListener listener)
	{
		if (topicListeners.add(listener))
		{
			log.info("Add listener -> '{}'", listener.toString());
		}
	}

	private void broadCastActionTopicConsumer(String action)
	{
		Set<Channel> sessions = Gcs.getManagedConnectorSessions();

		for (Channel channel : sessions)
		{
			try
			{
				broadCastTopicInfo(action, channel);
			}
			catch (Throwable t)
			{
				log.error(t.getMessage(), t);

				try
				{
					channel.close();
				}
				catch (Throwable ct)
				{
					log.error(ct.getMessage(), ct);
				}
			}
		}
	}

	private void broadCastNewTopicConsumer()
	{
		log.info("Tell all peers about new topic consumer for: '{}'", subscriptionName);
		broadCastActionTopicConsumer("CREATE");
	}

	private void broadCastRemovedTopicConsumer()
	{
		log.info("Tell all peers about deleted topic consumer of: '{}'", subscriptionName);
		broadCastActionTopicConsumer("DELETE");
	}

	protected void broadCastTopicInfo(String action, Channel channel)
	{
		InternalMessage msg = new InternalMessage();
		msg.setType(MessageType.SYSTEM_TOPIC);

		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
		String payload = String.format(ptemplate, action, GcsInfo.getAgentName(), ((InetSocketAddress) channel.getRemoteAddress()).getHostName(), subscriptionName);

		NetBrokerMessage brokerMsg;
		try
		{
			brokerMsg = new NetBrokerMessage(payload.getBytes("UTF-8"));
			brokerMsg.setMessageId(msg.getMessageId());
			brokerMsg.setTimestamp(msg.getTimestamp());
			brokerMsg.setExpiration(msg.getExpiration());
			msg.setDestination(subscriptionName);

			msg.setContent(brokerMsg);
		}
		catch (UnsupportedEncodingException e)
		{
			// This exception dosen't happen: "UTF-8" is built-in in every JVM
		}

		SystemMessagesPublisher.sendMessage(msg, channel);
	}

	public String getSubscriptionName()
	{
		return subscriptionName;
	}

	protected boolean hasLocalConsumers()
	{
		for (MessageListener l : topicListeners)
		{
			if (l.getType() == MessageListener.Type.LOCAL)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isBroadcastable()
	{
		return broadcastable;
	}

	public Set<MessageListener> listeners()
	{
		return topicListeners;
	}

	protected void notify(InternalMessage message, boolean localOnly)
	{
		if (size() > 0)
		{
			String topicName = message.getDestination();
			NetMessage nmsg = null;

			if (DestinationMatcher.match(subscriptionName, topicName))
			{
				for (MessageListener ml : topicListeners)
				{
					if (ml != null)
					{
						if (localOnly && (ml.getType() != MessageListener.Type.LOCAL))
						{
							continue;
						}

						if (ml.getType() == MessageListener.Type.LOCAL)
						{
							if (nmsg == null)
							{
								nmsg = Gcs.buildNotification(message, ml.getsubscriptionKey(), ml.getTargetDestinationType());
							}
							ml.onMessage(nmsg);
						}
						else
						{
							ml.onMessage(message);
						}

						message.setDestination(topicName); // -> Set the destination name, queue dispatchers change it.
					}
				}
			}
		}
	}

	public void remove(MessageListener listener)
	{
		if (listener != null)
		{
			synchronized (topicListeners)
			{
				if (topicListeners.remove(listener))
				{
					log.info("Removed listener -> '{}'", listener.toString());

					boolean has_local = hasLocalConsumers();

					if (!has_local && (listener.getType() == MessageListener.Type.LOCAL))
					{
						broadCastRemovedTopicConsumer();
					}
				}
			}
		}
	}

	protected int size()
	{
		return topicListeners.size();
	}
}
