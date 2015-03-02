package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import pt.com.broker.client.nio.events.connection.ConnectionEventListener;
import pt.com.broker.client.nio.events.connection.ConnectionStatusChangeEvent;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.server.ReconnectEvent;

public class ConnectionStatusChangeEventHandler extends ChannelDuplexHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionStatusChangeEventHandler.class);
	
	private List<ConnectionEventListener> connectionEventListeners;
	
	public ConnectionStatusChangeEventHandler(List<ConnectionEventListener> connectionEventListeners){
		Preconditions.checkNotNull(connectionEventListeners, "The event listeners cannot be null.");
		this.connectionEventListeners = connectionEventListeners;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if(evt instanceof ConnectionStatusChangeEvent){
			notifyListeners((ConnectionStatusChangeEvent) evt);
		} else {
			ctx.fireUserEventTriggered(evt);
		}
	}
	
	private void notifyListeners(ConnectionStatusChangeEvent event){
		log.debug("Status {} to host {}", event.getConnectionStatus(), event.getHostInfo().toString());
		for(ConnectionEventListener listener: connectionEventListeners){
			switch(event.getConnectionStatus()){
				case CLOSED:
					//Disconnecteds
					log.debug("Notifying listener for disconnected agent.");
					listener.disconnected(event.getHostInfo());
					break;
				case CONNECTING:
					//Disconnected
					break;
				case DISABLE:
					//Disconnected
					log.debug("Notifying listener for disconnected agent.");
					listener.disconnected(event.getHostInfo());
					break;
				case OPEN:
					//Connected
					log.debug("Notifying listener for connected agent.");
					listener.connected(event.getHostInfo());
					break;
				default:
					break;
			}
		}
	}

}
