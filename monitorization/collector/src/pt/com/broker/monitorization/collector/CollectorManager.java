package pt.com.broker.monitorization.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.concurrent.CustomExecutors;
import org.caudexorigo.jdbc.DbExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;

public class CollectorManager
{
	private static final Logger log = LoggerFactory.getLogger(CollectorManager.class);

	// private static BrokerClient brokerClient;
	private static List<BrokerClient> brokerClients = new ArrayList<BrokerClient>();

	// private static StatisticsCollector statisticsCollector;
	private static List<StatisticsCollector> statisticsCollectors = new ArrayList<StatisticsCollector>();

	private static AgentStatusCollector agentStatusCollector;
	// private static FaultsCollector faultsCollector;
	private static List<FaultsCollector> faultsCollectors = new ArrayList<FaultsCollector>();

	protected static final ScheduledExecutorService SCHED_EXEC = CustomExecutors.newScheduledThreadPool(3, "sched-exec");

	public static void init()
	{

		List<HostInfo> localAgents = ConfigurationInfo.getAgents();

		try
		{
			for (HostInfo hostInfo : localAgents)
			{
				brokerClients.add(new BrokerClient(hostInfo.getHostname(), hostInfo.getPort()));
			}
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
				log.info("Running Database cleaner. Delete old entries (events and faults)");

				// int del_events_counter = DbExecutor.runActionPreparedStatement("DELETE FROM raw_data WHERE (event_time < (now()-'00:30'::time));");

				int del_events_counter = 0;

				int del_faults_counter = DbExecutor.runActionPreparedStatement("DELETE FROM fault_data WHERE (event_time < (now()-'00:30'::time));");

				log.info("Database cleaner deleted {} event entries and {} fault entries.", del_events_counter, del_faults_counter);
			}
		};

		SCHED_EXEC.scheduleWithFixedDelay(db_cleaner, 1, 1, TimeUnit.MINUTES);
	}

	private static void initCollectors()
	{
		// Init statistics collector
		try
		{
			for (BrokerClient bk : brokerClients)
			{
				statisticsCollectors.add(new StatisticsCollector(bk));
			}
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
			for (BrokerClient bk : brokerClients)
			{
				faultsCollectors.add(new FaultsCollector(bk));
			}

		}
		catch (Throwable e)
		{
			throw new RuntimeException("Failed to create AgentStatusCollector", e);
		}

		try
		{
			for (StatisticsCollector sc : getStatisticsCollector())
			{
				sc.start();
			}
			for (FaultsCollector fc : getFaultsCollector())
			{
				fc.start();
			}

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

	public static List<StatisticsCollector> getStatisticsCollector()
	{
		return statisticsCollectors;
	}

	public static List<FaultsCollector> getFaultsCollector()
	{
		return faultsCollectors;
	}

	public static AgentStatusCollector getAgentStatusCollector()
	{
		return agentStatusCollector;
	}

	public static void scheduleWithFixedDelay(Runnable command, long initial_delay, long delay, TimeUnit unit)
	{
		SCHED_EXEC.scheduleWithFixedDelay(command, initial_delay, delay, unit);
	}

	public static void schedule(Runnable command, long delay, TimeUnit unit)
	{
		SCHED_EXEC.schedule(command, delay, unit);
	}
}
