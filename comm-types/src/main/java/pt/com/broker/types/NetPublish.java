package pt.com.broker.types;

/**
 * Represents a Publication message.
 * 
 */
public final class NetPublish
{
	private String actionId;
	private NetAction.DestinationType destinationType;
	private String destination;
	private NetBrokerMessage message;

	public NetPublish(String destination, NetAction.DestinationType destinationType, NetBrokerMessage message)
	{
		this.destinationType = destinationType;
		this.destination = destination;
		this.message = message;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public NetAction.DestinationType getDestinationType()
	{
		return destinationType;
	}

	public String getDestination()
	{
		return destination;
	}

	public NetBrokerMessage getMessage()
	{
		return message;
	}
}