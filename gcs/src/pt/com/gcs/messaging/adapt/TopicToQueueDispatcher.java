package pt.com.gcs.messaging.adapt;


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

	public boolean onMessage(Message message)
	{
		return true;
	}

	public String getDestinationName()
	{
		return _queueName;
	}
}
