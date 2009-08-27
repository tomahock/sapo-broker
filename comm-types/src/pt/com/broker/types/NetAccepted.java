package pt.com.broker.types;

/**
 * Represents an Accepted message.
 * 
 */

public final class NetAccepted
{
	private String actionId;

	public NetAccepted(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}
}
