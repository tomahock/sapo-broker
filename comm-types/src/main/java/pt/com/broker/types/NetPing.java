package pt.com.broker.types;

/**
 * Represents a Ping message.
 * 
 */
public class NetPing
{
	private String actionId;

	public NetPing(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}
}
