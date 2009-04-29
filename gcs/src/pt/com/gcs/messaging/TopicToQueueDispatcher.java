package pt.com.gcs.messaging;

import pt.com.types.NetAction.DestinationType;

class TopicToQueueDispatcher implements MessageListener
{

	private final String _queueName;

	public TopicToQueueDispatcher(String queueName)
	{
		_queueName = queueName;
	}

	@Override
	public DestinationType getDestinationType()
	{
		return DestinationType.TOPIC;
	}

	public boolean onMessage(InternalMessage message)
	{
		if (!message.isFromRemotePeer())
		{
			message.setDestination(_queueName);
			Gcs.enqueue(message);
			//Gcs.enqueue(message, _queueName);
		} 
		return true;
	}

	public String getDestinationName()
	{
		return _queueName;
	}
}
