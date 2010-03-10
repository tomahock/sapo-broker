package pt.com.broker.messaging;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.MessageListener;

/**
 * BrokerListener is a base class for types representing message consumers.
 * 
 */

public abstract class BrokerListener implements MessageListener
{
	protected final static int MAX_SESSION_BUFFER_SIZE = 128 * 1024;
	
	protected static NetMessage buildNotification(InternalMessage msg, DestinationType dtype)
	{
		return buildNotification(msg, null, dtype);
	}

	protected static NetMessage buildNotification(InternalMessage msg, String subscriptionName, DestinationType dtype)
	{
		NetNotification notification = new NetNotification(msg.getDestination(), dtype, msg.getContent(), subscriptionName);

		NetAction action = new NetAction(NetAction.ActionType.NOTIFICATION);
		action.setNotificationMessage(notification);

		notification.getMessage().setMessageId(msg.getMessageId());

		Map<String, String> params = new HashMap<String, String>();
		params.put("FROM", msg.getSourceApp());
		params.put("ACTION", "http://services.sapo.pt/broker/notification/" + msg.getMessageId());
		params.put("PUBLISHING_AGENT", msg.getPublishingAgent());
		
		NetMessage message = new NetMessage(action, params);

		return message;
	}


	public abstract int addConsumer(Channel channel, boolean ackRequired);

	public abstract int removeSessionConsumer(Channel channel);
}
