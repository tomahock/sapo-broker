package pt.com.gcs.messaging;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.ForwardResult.Result;

/**
 * TopicToQueueDispatcher is responsible for enqueueing topic messages that have durable subscribers registered.
 */

class TopicToQueueDispatcher implements MessageListener
{
	private static final ForwardResult success = new ForwardResult(Result.SUCCESS);

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
	public ForwardResult onMessage(InternalMessage message)
	{
		if (!message.isFromRemotePeer())
		{
			message.setDestination(destinationQueueName);
			Gcs.enqueue(message);
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
}
