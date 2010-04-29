package pt.com.broker.monitorization.collector;

import java.util.List;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;

public class CollectorManager
{
	private static BaseBrokerClient brokerClient; 

	private static StatisticsCollector statisticsCollector;
	private static AgentStatusCollector agentStatusCollector;
	private static FaultsCollector faultsCollector;
	
	public static void init()
	{
		
		List<HostInfo> localAgents = ConfigurationInfo.getAgents();
		
		try
		{
			brokerClient = new BrokerClient(localAgents);
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to connect to an agent", e);
		}
		
		initCollectors();
	}
	
	private static void initCollectors()
	{
		// Init statistics collector
		try
		{
			statisticsCollector = new StatisticsCollector( brokerClient );
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create SubscriptionCountCollector", e);
		}
		
		// Init agent status collector;
		try
		{
			agentStatusCollector = new AgentStatusCollector();
			
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create AgentStatusCollector", e);
		}
		
		try
		{
			faultsCollector = new FaultsCollector(brokerClient);
			
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create AgentStatusCollector", e);
		}
		
		
		try
		{
			getStatisticsCollector().start();
			getFaultsCollector().start();
			getAgentStatusCollector().start();
		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to init a collector", e);
		}
	}


	public static void stop()
	{
		
	}

	public static StatisticsCollector getStatisticsCollector()
	{
		return statisticsCollector;
	}
	
	public static FaultsCollector getFaultsCollector()
	{
		return faultsCollector;
	}

	public static AgentStatusCollector getAgentStatusCollector()
	{
		return agentStatusCollector;
	}
}
