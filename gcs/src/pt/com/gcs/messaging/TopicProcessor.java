package pt.com.gcs.messaging;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;

import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.MessageListener.Type;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.channels.ListenerChannel;
import pt.com.gcs.conf.GcsInfo;

public class TopicProcessor
{
	private static Logger log = LoggerFactory.getLogger(TopicProcessor.class);

	private static final Charset UTF8 = Charset.forName("UTF-8");

	private final TopicStatistics topicStatistics = new TopicStatistics();

	private final String subscriptionName;

	private final Set<MessageListener> topicListeners = new CopyOnWriteArraySet<MessageListener>();

	volatile private boolean broadcastable;

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
		boolean has_local = false;
		if (listener.getType() == MessageListener.Type.LOCAL)
		{
			// this value is just important if current listener is local.
			has_local = hasLocalConsumers();
		}

		if (topicListeners.add(listener))
		{
			log.info("Add listener -> '{}'", listener.toString());

			if (listener.getType() == MessageListener.Type.LOCAL)
			{
				// before adding current local listener it didn't had local listeners, so notify other agents
				if (!has_local)
				{
					broadCastNewTopicConsumer();
				}
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
		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
		String payload = String.format(ptemplate, action, GcsInfo.getAgentName(), ((InetSocketAddress) channel.getRemoteAddress()).getHostName(), subscriptionName);

		NetBrokerMessage brkMsg = new NetBrokerMessage(payload.getBytes(UTF8));
		brkMsg.setMessageId(MessageId.getMessageId());

		NetNotification notification = new NetNotification("/system/peer", DestinationType.TOPIC, brkMsg, "/system/peer");

		NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
		naction.setNotificationMessage(notification);

		NetMessage nmsg = new NetMessage(naction);
		nmsg.getHeaders().put("TYPE", "SYSTEM_TOPIC");

		SystemMessagesPublisher.sendMessage(nmsg, channel);
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

	protected void notify(NetPublish np, boolean localOnly, Set<ListenerChannel> remoteListenersTouched)
	{
		if (size() > 0)
		{
			String topicName = np.getDestination();

			if (DestinationMatcher.match(subscriptionName, topicName))
			{
				NetMessage nmsg = Gcs.buildNotification(np, subscriptionName);
				for (MessageListener ml : topicListeners)
				{
					if (ml != null)
					{
						if (localOnly && (ml.getType() == MessageListener.Type.REMOTE))
						{
							continue;
						}
						else
						{
							if (ml.getType() == Type.REMOTE)
							{
								if (!remoteListenersTouched.contains(ml.getChannel()))
								{
									doNotify(nmsg, ml);
									remoteListenersTouched.add(ml.getChannel());
								}
							}
							else
							{
								doNotify(nmsg, ml);
							}

						}
					}
				}
			}
		}
	}

	private void doNotify(NetMessage nmsg, MessageListener ml)
	{
		if (ml.onMessage(nmsg).result == ForwardResult.Result.SUCCESS)
		{
			if ((ml.getTargetDestinationType() == DestinationType.TOPIC) && ml.getType().equals(MessageListener.Type.LOCAL))
			{
				topicStatistics.newTopicMessageDelivered();
			}
			else
			{
				topicStatistics.newTopicDispatchedToQueueMessage();
			}
		}
		else
		{
			topicStatistics.newTopicDiscardedMessage();
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

	public TopicStatistics getTopicStatistics()
	{
		return topicStatistics;
	}
}
