package pt.com.broker.monitorization.consolidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pt.com.broker.monitorization.collectors.CollectorManager;
import pt.com.broker.monitorization.collectors.FaultListener;
import pt.com.broker.monitorization.collectors.QueueSizeListener;
import pt.com.broker.monitorization.collectors.SubscriptionCountListener;

public class ConsolidatorManager
{
	private static Map<String, Agent> agents = new HashMap<String, Agent>();
	
	public static void init()
	{
		initConsolidators();
	}
	
	private static void initConsolidators()
	{
		// Init subscription count collector
		
		
		
		CollectorManager.getSubscriptionCountCollector().addListener(new SubscriptionCountListener(){
			@Override
			public void onUpdate(String agentName, String subscriptionType, String subscriptionName, int count)
			{
//				String s = String.format("SubscriptionCountListener -- Agent: %s, Destination type: %s, Subscription name: %s, Count: %s", agentName, subscriptionType.toString(),subscriptionName, count+"");
//				System.out.println(s);
//				Agent agent = getAgent(agentName);
//				Subscription subscription = agent.getSubscription(subscriptionType, subscriptionName);
//				subscription.setCount(count);
			}
		});

		// Init queue size collector

		CollectorManager.getQueueSizeCollector().addListener( new QueueSizeListener(){
			@Override
			public void onUpdate(String agentName, String queueName, int size)
			{
				//String s = String.format("QueueSizeListener -- Agent: %s, Queue name: %s, Size: %s", agentName, queueName, size+"");
				//System.out.println(s);					
				Agent agent = getAgent(agentName);
				Queue queue = agent.getQueue(queueName);
				queue.setCount(size);
			}
		});
		
		// Init faults collector
		CollectorManager.getFaultsCollector().addListener( new FaultListener()
		{
			@Override
			public void onFault(String agentName, String message)
			{
				Agent agent = getAgent(agentName);
				Fault fault = new Fault(agent, message, System.currentTimeMillis());
				agent.addFault(fault);
			}
		});
	}
	
	public static Agent getAgent(String agentName)
	{
		Agent agent = null;
		synchronized (agents)
		{
			agent = agents.get(agentName);
			if(agent == null)
			{
				agent = new Agent(agentName);
				agents.put(agentName, agent);
			}
		}
		return agent;
	}
	
	public static Collection<Agent> getAgents()
	{
		synchronized (agents)
		{
			return agents.values();
		}
	}

	public static void stop()
	{
				
	}
}
