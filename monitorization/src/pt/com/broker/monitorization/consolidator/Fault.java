package pt.com.broker.monitorization.consolidator;

import java.util.Date;

public class Fault
{
	private final Agent agent;
	private final String message;
	private final long time;

	public Fault(Agent agent, String message, long time)
	{
		this.agent = agent;
		this.message = message;
		this.time = time;
	}

	public Agent getAgent()
	{
		return agent;
	}

	public String getMessage()
	{
		return message;
	}

	public String getTime()
	{
		return new Date(time).toString() ;
	}
}
