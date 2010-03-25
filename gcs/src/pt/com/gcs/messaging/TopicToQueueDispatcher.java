package pt.com.gcs.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.DeliverableMessage;
import pt.com.broker.types.ForwardResult;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.ForwardResult.Result;
import pt.com.broker.types.NetAction.DestinationType;

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

		InternalMessage imsg;
		if (msg instanceof InternalMessage)
		{
			imsg = (InternalMessage) msg;
		}
		else
		{
			log.warn("Don't know how to handle this message type");
			return failed;
		}

		if (!imsg.isFromRemotePeer())
		{
			imsg.setDestination(destinationQueueName);
			Gcs.enqueue(imsg);
		}
		return success;
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
}
