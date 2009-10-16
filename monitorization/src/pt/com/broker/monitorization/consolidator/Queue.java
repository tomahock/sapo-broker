package pt.com.broker.monitorization.consolidator;

import java.util.concurrent.atomic.AtomicInteger;

public class Queue
{
	private final Agent agent;
	private final String name;

	private AtomicInteger count = new AtomicInteger(0);

	public Queue(Agent agent, String name)
	{
		this.agent = agent;
		this.name = name;
	}

	public Agent getAgent()
	{
		return agent;
	}

	public String getName()
	{
		return name;
	}

	public void setCount(int count)
	{
		this.count.set(count);
	}

	public int getCount()
	{
		return this.count.get();
	}
}
