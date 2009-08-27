package pt.com.broker.types;

/**
 * Represents a Subscription message.
 * 
 */

public final class NetSubscribe
{
	private String actionId;
	private String destination;
	private NetAction.DestinationType destinationType;

	public NetSubscribe(String destination, NetAction.DestinationType destinationType)
	{
		this.destination = destination;
		this.destinationType = destinationType;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public String getDestination()
	{
		return destination;
	}

	public NetAction.DestinationType getDestinationType()
	{
		return destinationType;
	}
}
