package pt.com.broker.types;

public final class NetPoll
{
	private String actionId;
	private String destination;

	public NetPoll(String destination)
	{
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

	public String getDestination()
	{
		return destination;
	}

}