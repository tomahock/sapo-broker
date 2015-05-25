package pt.com.gcs.stats;

public class SystemStatistics
{

	private Long invalidMessages;
	private Long accessDeniedMessages;
	private Long failedMessages;
	private Long tcpConnections;
	private Long tcpLegacyConnections;
	private Long sslConnections;
	private Long systemFaults;
	private Long receivedTopicMessages;

	public Long getInvalidMessages()
	{
		return invalidMessages;
	}

	public void setInvalidMessages(Long invalidMessages)
	{
		this.invalidMessages = invalidMessages;
	}

	public Long getAccessDeniedMessages()
	{
		return accessDeniedMessages;
	}

	public void setAccessDeniedMessages(Long accessDeniedMessages)
	{
		this.accessDeniedMessages = accessDeniedMessages;
	}

	public Long getFailedMessages()
	{
		return failedMessages;
	}

	public void setFailedMessages(Long failedMessages)
	{
		this.failedMessages = failedMessages;
	}

	public Long getTcpConnections()
	{
		return tcpConnections;
	}

	public void setTcpConnections(Long tcpConnections)
	{
		this.tcpConnections = tcpConnections;
	}

	public Long getTcpLegacyConnections()
	{
		return tcpLegacyConnections;
	}

	public void setTcpLegacyConnections(Long tcpLegacyConnections)
	{
		this.tcpLegacyConnections = tcpLegacyConnections;
	}

	public Long getSslConnections()
	{
		return sslConnections;
	}

	public void setSslConnections(Long sslConnections)
	{
		this.sslConnections = sslConnections;
	}

	public Long getSystemFaults()
	{
		return systemFaults;
	}

	public void setSystemFaults(Long systemFaults)
	{
		this.systemFaults = systemFaults;
	}

	public Long getReceivedTopicMessages()
	{
		return receivedTopicMessages;
	}

	public void setReceivedTopicMessages(Long receivedTopicMessages)
	{
		this.receivedTopicMessages = receivedTopicMessages;
	}

}
