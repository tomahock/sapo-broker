package pt.com.broker.client.nio.events.connection;

import pt.com.broker.client.nio.server.HostInfo;

public interface ConnectionStatusChangeEvent
{

	public HostInfo getHostInfo();

	public HostInfo.STATUS getConnectionStatus();

}
