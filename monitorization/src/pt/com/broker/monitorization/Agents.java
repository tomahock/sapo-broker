package pt.com.broker.monitorization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import pt.com.broker.client.HostInfo;

public class Agents
{
	private final static Map<String, AgentInfo> agents = new HashMap<String, AgentInfo>();
	
	
	public static void addAgent(AgentInfo agent)
	{
		synchronized (AgentInfo.class)
		{
			agents.put(agent.getName(), agent);
		}
	}
	
	public static AgentInfo getAgentInfo(String name)
	{
		synchronized (AgentInfo.class)
		{
			return agents.get(name);
		}
	}
	
	public Collection<AgentInfo> getAgentsInfo()
	{
		synchronized (AgentInfo.class)
		{
			return agents.values();
		}
	}
	
	
	static
	{
		//TODO: This information must be obtained some other way 
		addAgent(new AgentInfo("broker1", new HostInfo("localhost", 3323)));
		addAgent(new AgentInfo("broker2", new HostInfo("localhost", 3423)));
	}
	
}
