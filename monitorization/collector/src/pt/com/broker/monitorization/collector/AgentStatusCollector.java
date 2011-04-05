package pt.com.broker.monitorization.collector;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;
import pt.com.broker.monitorization.configuration.ConfigurationInfo.AgentInfo;
import pt.com.broker.monitorization.db.StatisticsDB;
import pt.com.broker.types.NetPong;

public class AgentStatusCollector
{
	private Runnable statusVerifier;

	public enum AgentStatus
	{
		Ok, Down
	}

	public void start() throws Throwable
	{
		statusVerifier = new Runnable()
		{
			public void run()
			{
				List<AgentInfo> cloudAgents = ConfigurationInfo.getCloudAgents();
				for (AgentInfo agent : cloudAgents)
				{
					BrokerClient bk = null;
					NetPong checkStatus = null;
					try
					{
						bk = new BrokerClient(agent.tcpInfo.getHostname(), agent.tcpInfo.getPort(), 0);
						checkStatus = bk.checkStatus();
					}
					catch (Throwable t)
					{
					}
					processResult(agent.hostname, (checkStatus != null) ? AgentStatus.Ok : AgentStatus.Down);
					if (bk != null)
						bk.close();
				}
			}
		};

		CollectorManager.scheduleWithFixedDelay(statusVerifier, 30, 60, TimeUnit.SECONDS);
	}

	private void processResult(String agentName, AgentStatus status)
	{
		final String SUBJECT = "agent";
		final String PREDICATE = "status";

		StatisticsDB.add(agentName, new Date(), SUBJECT, PREDICATE, (status == AgentStatus.Ok) ? 1 : 0);
	}
}
