package pt.com.broker.monitorization;

import pt.com.broker.client.HostInfo;

public class AgentInfo
{
	private final HostInfo hostInfo;
	private final String name;
	
	public AgentInfo(String name, HostInfo hostInfo)
	{
		this.name = name;
		this.hostInfo = hostInfo;		
	}

	public HostInfo getHostInfo()
	{
		return hostInfo;
	}

	public String getName()
	{
		return name;
	}
}
