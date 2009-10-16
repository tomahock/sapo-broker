package pt.com.broker.monitorization.consolidator;

import java.util.concurrent.atomic.AtomicInteger;

import pt.com.broker.types.NetAction.DestinationType;

public class Subscription
{
	private final Agent agent;
	private final DestinationType subscriptionType;
	private final String subscriptionName;

	private AtomicInteger count = new AtomicInteger(0);

	public Subscription(Agent agent, DestinationType subscriptionType, String subscriptionName)
	{
		this.agent = agent;
		this.subscriptionType = subscriptionType;
		this.subscriptionName = subscriptionName;
	}

	public Agent getAgent()
	{
		return agent;
	}

	public DestinationType getSubscriptionType()
	{
		return subscriptionType;
	}

	public String getSubscriptionName()
	{
		return subscriptionName;
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
