package pt.com.gcs.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.DeliverableMessage;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.MessageListenerEventChangeHandler;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.channels.ListenerChannel;

/**
 * TopicToQueueDispatcher is responsible for enqueueing topic messages that have durable subscribers registered.
 */

class TopicToQueueDispatcher implements MessageListener
{
	private static final Logger log = LoggerFactory.getLogger(TopicToQueueDispatcher.class);

	private static final ForwardResult success = new ForwardResult(Result.SUCCESS);
	private static final ForwardResult failed = new ForwardResult(Result.FAILED);

	private final String destinationQueueName;
	private final String topicSubscriptionKey;

	public TopicToQueueDispatcher(ListenerChannel lchannel, String topicSubscriptionKey, String destinationQueueName)
	{
		this.topicSubscriptionKey = topicSubscriptionKey;
		this.destinationQueueName = destinationQueueName;
	}

	@Override
	public DestinationType getSourceDestinationType()
	{
		return DestinationType.TOPIC;
	}

	@Override
	public DestinationType getTargetDestinationType()
	{
		return DestinationType.QUEUE;
	}

	@Override
	public ForwardResult onMessage(DeliverableMessage msg)
	{
		if (msg instanceof NetMessage)
		{
			NetMessage nmsg = (NetMessage) msg;
			NetNotification nnot_orig = nmsg.getAction().getNotificationMessage();

			NetNotification nnot_fwd = new NetNotification(destinationQueueName, DestinationType.QUEUE, nnot_orig.getMessage(), destinationQueueName);
			NetAction action_fwd = new NetAction(NetAction.ActionType.NOTIFICATION);
			action_fwd.setNotificationMessage(nnot_fwd);

			NetMessage nmsg_fwd = new NetMessage(action_fwd);
			nmsg_fwd.getHeaders().putAll(nmsg.getHeaders());

			nmsg_fwd.getHeaders().put("ORIGINAL_DESTINATION", nnot_orig.getDestination());

			if (!isFromRemotePeer(nmsg))
			{
				Gcs.enqueue(nmsg_fwd, destinationQueueName);
				return success;
			}
		}
		else
		{
			log.error("Don't know how to handle message");
		}
		return failed;
	}

	private boolean isFromRemotePeer(NetMessage nmsg)
	{
		if (nmsg.getHeaders() != null)
		{
			return Boolean.parseBoolean(nmsg.getHeaders().get("IS_REMOTE"));
		}

		return false;

	}

	@Override
	public String getsubscriptionKey()
	{
		return topicSubscriptionKey;
	}

	@Override
	public boolean isReady()
	{
		return true;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public ListenerChannel getChannel()
	{
		// There is no underlying channel in a TopicToQueueDispatcher
		return null;
	}

	@Override
	public boolean isAckRequired()
	{
		return false;
	}

	@Override
	public Type getType()
	{
		return MessageListener.Type.INTERNAL;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationQueueName == null) ? 0 : destinationQueueName.hashCode());
		result = prime * result + ((topicSubscriptionKey == null) ? 0 : topicSubscriptionKey.hashCode());
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
		TopicToQueueDispatcher other = (TopicToQueueDispatcher) obj;
		if (destinationQueueName == null)
		{
			if (other.destinationQueueName != null)
				return false;
		}
		else if (!destinationQueueName.equals(other.destinationQueueName))
			return false;
		if (topicSubscriptionKey == null)
		{
			if (other.topicSubscriptionKey != null)
				return false;
		}
		else if (!topicSubscriptionKey.equals(other.topicSubscriptionKey))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "TopicToQueueDispatcher [type=" + getType().toString() + ", destinationQueueName=" + destinationQueueName + ", topicSubscriptionKey=" + topicSubscriptionKey + "]";
	}

	@Override
	public void addStateChangeListener(MessageListenerEventChangeHandler handler)
	{
		// No change state will ever exist, so there is nothing to do.
	}

	@Override
	public void removeStateChangeListener(MessageListenerEventChangeHandler handler)
	{
		// No change state will ever exist, so there is nothing to do.
	}
}
