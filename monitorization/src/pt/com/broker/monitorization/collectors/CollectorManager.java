package pt.com.broker.monitorization.collectors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.monitorization.consolidator.Agent;
import pt.com.broker.monitorization.consolidator.Fault;
import pt.com.broker.monitorization.consolidator.Queue;
import pt.com.broker.monitorization.consolidator.Subscription;
import pt.com.broker.types.NetAction.DestinationType;

public class CollectorManager
{
	private static String worldmapLocation;
	private static BaseBrokerClient brokerClient; 

	private static Map<String, Agent> agents = new HashMap<String, Agent>();
	
	private static SubscriptionCountCollector subscriptionCountCollector;
	private static QueueSizeCollector queueSizeCollector;
	private static FaultsCollector faultsCollector;
	private static DropboxCollector dropboxCollector;
	
	public static void init(String worldmapLocation)
	{
		CollectorManager.worldmapLocation = worldmapLocation;
		
		/* This shouldn't be hard-coded */
		HostInfo hostInfo = new HostInfo("broker.bk.sapo.pt", 3323);
		//HostInfo hostInfo = new HostInfo("127.0.0.1", 3323);
		
		try
		{
			brokerClient = new BrokerClient(hostInfo.getHostname(), hostInfo.getPort());
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to connect to an agent", e);
		}
		
		initCollectors();
	}
	
	private static void initCollectors()
	{
		// Init subscription count collector
		try
		{
			subscriptionCountCollector = new SubscriptionCountCollector( brokerClient );
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create SubscriptionCountCollector", e);
		}
		

		// Init queue size collector
		try
		{
			queueSizeCollector = new QueueSizeCollector( brokerClient );
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create QueueSizeCollector", e);
		}

		
		// Init faults collector
		try
		{
			faultsCollector = new FaultsCollector(brokerClient);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create FaultsCollector", e);
		}
		
		// Init dropbox collector
		try
		{
			dropboxCollector = new DropboxCollector(brokerClient);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create DropboxCollector", e);
		}
		
		try
		{
			getSubscriptionCountCollector().start();
			getQueueSizeCollector().start();
			getFaultsCollector().start();
			getDropboxCollector().start();
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to init a collector", e);
		}
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

	public static SubscriptionCountCollector getSubscriptionCountCollector()
	{
		return subscriptionCountCollector;
	}

	public static QueueSizeCollector getQueueSizeCollector()
	{
		return queueSizeCollector;
	}

	public static FaultsCollector getFaultsCollector()
	{
		return faultsCollector;
	}

	public static DropboxCollector getDropboxCollector()
	{
		return dropboxCollector;
	}

}
