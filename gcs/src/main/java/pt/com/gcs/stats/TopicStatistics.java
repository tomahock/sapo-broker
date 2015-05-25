package pt.com.gcs.stats;

public class TopicStatistics
{

	private String topicName;
	private Long deliveredMessages;
	private Long discardedMessages;
	private Long dispatchedToQueueMessages;

	public String getTopicName()
	{
		return topicName;
	}

	public void setTopicName(String topicName)
	{
		this.topicName = topicName;
	}

	public Long getDeliveredMessages()
	{
		return deliveredMessages;
	}

	public void setDeliveredMessages(Long deliveredMessages)
	{
		this.deliveredMessages = deliveredMessages;
	}

	public Long getDiscardedMessages()
	{
		return discardedMessages;
	}

	public void setDiscardedMessages(Long discardedMessages)
	{
		this.discardedMessages = discardedMessages;
	}

	public Long getDispatchedToQueueMessages()
	{
		return dispatchedToQueueMessages;
	}

	public void setDispatchedToQueueMessages(Long dispatchedToQueueMessages)
	{
		this.dispatchedToQueueMessages = dispatchedToQueueMessages;
	}

}
