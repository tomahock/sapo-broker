package pt.com.broker.client.nio.events.connection;

import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.server.HostInfo.STATUS;

/**
 * The ConnectionStatusChangeEvent represents a connection status change. This events are triggered when a connection state change event occurs and are delivered to any listeners added to the client.
 * */
public class ConnectionStatusChangeEventImpl implements ConnectionStatusChangeEvent
{

	private final HostInfo hostInfo;
	private final HostInfo.STATUS connectionStatus;

	public ConnectionStatusChangeEventImpl(HostInfo hostInfo, HostInfo.STATUS connectionStatus)
	{
		this.hostInfo = hostInfo;
		this.connectionStatus = connectionStatus;
	}

	@Override
	public HostInfo getHostInfo()
	{
		return hostInfo;
	}

	@Override
	public STATUS getConnectionStatus()
	{
		return connectionStatus;
	}

}
