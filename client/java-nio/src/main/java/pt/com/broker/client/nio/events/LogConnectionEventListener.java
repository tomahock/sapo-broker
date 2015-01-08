package pt.com.broker.client.nio.events;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import pt.com.broker.client.nio.server.HostInfo;

/**
 * Simple Connection event listener for logging when the connection state changes.
 * */
public class LogConnectionEventListener implements ConnectionEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(LogConnectionEventListener.class);

	@Override
	public void connected(HostInfo hostInfo) {
		log.debug("**************************Successfully connected to host {}:{}**************************", hostInfo.getHostname(), hostInfo.getPort());
	}

	@Override
	public void disconnected(HostInfo hostInfo) {
		log.debug("**************************Connection to host {}:{} lost.**************************", hostInfo.getHostname(), hostInfo.getPort());
	}

	@Override
	public void reconnected(HostInfo hostInfo) {
		log.debug("**************************Reconnected to host {}:{}.**************************", hostInfo.getHostname(), hostInfo.getPort());
	}

}
