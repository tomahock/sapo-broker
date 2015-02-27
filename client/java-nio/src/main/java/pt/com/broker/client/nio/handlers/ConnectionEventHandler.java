package pt.com.broker.client.nio.handlers;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import pt.com.broker.client.nio.events.ConnectionEventListener;
import pt.com.broker.client.nio.events.DisconnectedEvent;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.server.ReconnectEvent;

public class ConnectionEventHandler extends ChannelDuplexHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectionEventHandler.class);
	
	private List<ConnectionEventListener> connectionEventListeners;
	
	public ConnectionEventHandler(List<ConnectionEventListener> connectionEventListeners){
		Preconditions.checkNotNull(connectionEventListeners, "The event listeners cannot be null.");
		this.connectionEventListeners = connectionEventListeners;
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		if(evt instanceof ReconnectEvent){
			for(ConnectionEventListener listener: connectionEventListeners){
				listener.reconnected(((ReconnectEvent) evt).getHost());
			}
		} else if (evt instanceof DisconnectedEvent){
			
		} else {
			ctx.fireUserEventTriggered(evt);
		}
	}
	
	private void notifyListeners(HostInfo hostInfo){
		for(ConnectionEventListener listener: connectionEventListeners){
//			listener.reconnected(((ReconnectEvent) evt).getHost());
		}
	}

}
