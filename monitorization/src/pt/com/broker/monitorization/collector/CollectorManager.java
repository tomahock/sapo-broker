package pt.com.broker.monitorization.collector;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.jdbc.DbExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;

public class CollectorManager
{
	private static final Logger log = LoggerFactory.getLogger(CollectorManager.class);

	private static BaseBrokerClient brokerClient;

	private static StatisticsCollector statisticsCollector;
	private static AgentStatusCollector agentStatusCollector;
	private static FaultsCollector faultsCollector;

	private static final ScheduledExecutorService sched_exec = CustomExecutors.newScheduledThreadPool(2, "sched-exec");

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

		Runnable db_cleaner = new Runnable()
		{
			public void run()
			{
				log.info("Running Database cleaner. Delete old entries");

				DbExecutor.runActionPreparedStatement("DELETE FROM raw_data WHERE (predicate='output-rate' OR predicate='input-rate') AND (object_value=0) AND (event_time < (now()-'00:10'::time));");

				DbExecutor.runActionPreparedStatement("DELETE FROM raw_data WHERE (event_time < (now()-'00:20'::time));");
			}
		};

		sched_exec.scheduleWithFixedDelay(db_cleaner, 1, 1, TimeUnit.MINUTES);
	}

	private static void initCollectors()
	{
		// Init statistics collector
		try
		{
			statisticsCollector = new StatisticsCollector(brokerClient);
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
