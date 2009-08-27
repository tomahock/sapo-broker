package pt.com.broker.types;

public final class NetPoll
{
	private String actionId;
	private String destination;
	private long timeout;
	private long expires;

	public NetPoll(String destination, long timeout)
	{
		this.destination = destination;
		this.timeout = timeout;
		this.expires = System.currentTimeMillis() + timeout;
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

	public long getTimeout()
	{
		return timeout;
	}

	public long getExpires()
	{
		return expires;
	}

	public boolean expired()
	{
		return System.currentTimeMillis() > expires;
	}
}