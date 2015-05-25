package pt.com.gcs.net.stats;

import io.netty.channel.Channel;

public class AgentConnectionInfo
{

	private final String agentName;
	private final Channel channel;

	public AgentConnectionInfo(String agentName, Channel channel)
	{
		this.agentName = agentName;
		this.channel = channel;
	}

	public String getAgentName()
	{
		return agentName;
	}

	public Channel getChannel()
	{
		return channel;
	}

}
