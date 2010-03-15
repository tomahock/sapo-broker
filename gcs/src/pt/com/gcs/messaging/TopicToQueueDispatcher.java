package pt.com.gcs.messaging;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.ForwardResult.Result;

/**
 * TopicToQueueDispatcher is responsible for enqueueing topic messages that have durable subscribers registered.
 * 
 */

class TopicToQueueDispatcher implements MessageListener
{
	private final String _queueName;

	public TopicToQueueDispatcher(String queueName)
	{
		_queueName = queueName;
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

	private static final ForwardResult success = new ForwardResult(Result.SUCCESS);
	
	public ForwardResult onMessage(InternalMessage message)
	{
		if (!message.isFromRemotePeer())
		{
			message.setDestination(_queueName);
			Gcs.enqueue(message);
			// Gcs.enqueue(message, _queueName);
		}
		return success;
	}

	public String getDestinationName()
	{
		return _queueName;
	}

	@Override
	public boolean ready()
	{
		return true;
	}

	@Override
	public boolean isActive()
	{
		return true;
	}


}
