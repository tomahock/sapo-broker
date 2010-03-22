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
	public String toString()
	{
		return "TopicToQueueDispatcher [type=" + getType().toString() + ", destinationQueueName=" + destinationQueueName + ", topicSubscriptionKey=" + topicSubscriptionKey + "]";
	}
}
