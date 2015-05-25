package pt.com.gcs.stats;

public class QueueStatistics
{

	private String queueName;
	private Long receivedMessages;
	private Long deliveredMessages;
	private Long expiredMessages;
	private Long redeliveredMessages;

	public String getQueueName()
	{
		return queueName;
	}

	public void setQueueName(String queueName)
	{
		this.queueName = queueName;
	}

	public Long getReceivedMessages()
	{
		return receivedMessages;
	}

	public void setReceivedMessages(Long receivedMessages)
	{
		this.receivedMessages = receivedMessages;
	}

	public Long getDeliveredMessages()
	{
		return deliveredMessages;
	}

	public void setDeliveredMessages(Long deliveredMessages)
	{
		this.deliveredMessages = deliveredMessages;
	}

	public Long getExpiredMessages()
	{
		return expiredMessages;
	}

	public void setExpiredMessages(Long expiredMessages)
	{
		this.expiredMessages = expiredMessages;
	}

	public Long getRedeliveredMessages()
	{
		return redeliveredMessages;
	}

	public void setRedeliveredMessages(Long redeliveredMessages)
	{
		this.redeliveredMessages = redeliveredMessages;
	}

}
