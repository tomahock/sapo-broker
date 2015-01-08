package pt.com.broker.client.nio.events;

import java.util.EventListener;

import pt.com.broker.client.nio.server.HostInfo;

public interface ConnectionEventListener extends EventListener {
	
	public void reconnected(HostInfo hostInfo);
	
	public void connected(HostInfo hostInfo);
	
	public void disconnected(HostInfo hostInfo);

}
