package pt.com.broker.types;

/**
 * Represents an Acknowledge message.
 * 
 */

public final class NetAcknowledge
{
	private String actionId;
	private String messageId;
	private String destination;

	public NetAcknowledge(String destination, String messageId)
	{
		this.messageId = messageId;
		this.destination = destination;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public String getMessageId()
	{
		return messageId;
	}

	public String getDestination()
	{
		return destination;
	}

}