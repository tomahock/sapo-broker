package pt.com.broker.monitorization.consolidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import pt.com.broker.types.NetAction.DestinationType;

public class Agent
{
	private final String name;

	private final Map<String, Queue> queues = new HashMap<String, Queue>();
	private final Map<DestinationType, Map<String, Subscription> > subscriptions = new TreeMap<DestinationType, Map<String, Subscription> >();
	
	private final LinkedList<Fault> faults = new LinkedList<Fault>();
	private final int MAX_FAULT_MESSAGES = 20;
	
	public Agent(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}

	public Queue getQueue(String queueName)
	{
		Queue queue;
		synchronized (queues)
		{
			queue = queues.get(queueName);
			if( queue == null)
			{
				queue = new Queue(this, queueName);
				queues.put(queueName, queue);
			}
		}
		return queue;
	}
	
	public Collection<Queue> getQueues()
	{
		return queues.values();
	}
	
	public void removeQueue(String queueName)
	{
		throw new RuntimeException("Agent.removeQueue() not implemented");
	}
	
	public Subscription getSubscription(DestinationType destinationType, String subscriptionName)
	{
		Subscription subscription = null;
		synchronized (subscriptions)
		{
			Map<String, Subscription> subscriptionsMap = subscriptions.get(destinationType);
			if(subscriptionsMap == null)
			{
				subscriptionsMap = new TreeMap<String, Subscription>();
				subscriptions.put(destinationType, subscriptionsMap);
			}
			subscription = subscriptionsMap.get(subscriptionName);
			if(subscription == null)
			{
				subscription = new Subscription(this, destinationType, subscriptionName);
				subscriptionsMap.put(subscriptionName, subscription);
			}
		}
		return subscription;
	}
	
	public Collection<Subscription> getSubscriptions()
	{
		ArrayList<Subscription> agentSubscriptions = new ArrayList<Subscription>();
		synchronized (subscriptions)
		{
			for(Map<String, Subscription> pair : subscriptions.values())
			{
				agentSubscriptions.addAll(pair.values());
			}
		}
		
		return agentSubscriptions;
	}
	
	public void addFault(Fault fault)
	{
		synchronized (faults)
		{
			if(faults.size() == MAX_FAULT_MESSAGES)
				faults.removeFirst();
			faults.add(fault);			
		}
	}
	
	public Collection<Fault> getFaults()
	{
		return faults;
	}
}
