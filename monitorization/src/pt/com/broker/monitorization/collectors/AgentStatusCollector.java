package pt.com.broker.monitorization.collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.monitorization.Utils;
import pt.com.broker.monitorization.configuration.ConfigurationInfo;
import pt.com.broker.types.NetPong;

public class AgentStatusCollector
{
	private Runnable statusVerifier;

	private List<AgentStatusListener> listeners = new ArrayList<AgentStatusListener>();
	
	public AgentStatusCollector() throws Throwable
	{
		
	}

	public void start() throws Throwable
	{
		statusVerifier = new Runnable()
		{
			public void run()
			{
				if( listeners.size() == 0)
					return;
				
				List<HostInfo> cloudAgents = ConfigurationInfo.getCloudAgents();

				for (HostInfo agent : cloudAgents)
				{
					BrokerClient bk = null;
					try
					{
						bk = new BrokerClient(agent.getHostname(), agent.getPort());
						NetPong checkStatus = bk.checkStatus();
						notifyListeners( agent.getHostname()+":"+ agent.getPort(), (checkStatus != null) ? AgentStatus.Ok : AgentStatus.Down);						
					}
					catch (Throwable t)
					{

					}
					if(bk != null)
						bk.close();
				}
			}
		};

		Utils.schedule(statusVerifier, 30, 30, TimeUnit.SECONDS);
	}
	
	private void notifyListeners(String agentName, AgentStatus status)
	{
		synchronized (listeners)
		{
			for(AgentStatusListener listener : listeners)
				listener.onUpdate(agentName, status);
		}
	}
	
	public void addListener(AgentStatusListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}
}
