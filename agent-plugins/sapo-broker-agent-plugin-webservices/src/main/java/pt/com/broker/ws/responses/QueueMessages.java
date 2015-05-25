package pt.com.broker.ws.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueueMessages
{

	public static final String JSON_PROP_QUEUE_NAME = "queue";
	public static final String JSON_PROP_MESSAGES_LIST = "messages";

	@JsonProperty(JSON_PROP_QUEUE_NAME)
	private String queueName;
	@JsonProperty(JSON_PROP_MESSAGES_LIST)
	private MessageList messagesList;

	public QueueMessages()
	{

	}

	public QueueMessages(String queueName, MessageList messagesList)
	{
		this.queueName = queueName;
		this.messagesList = messagesList;
	}

	public String getQueueName()
	{
		return queueName;
	}

	public MessageList getMessagesList()
	{
		return messagesList;
	}

}
