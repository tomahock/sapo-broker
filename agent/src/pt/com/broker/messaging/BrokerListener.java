package pt.com.broker.messaging;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.MessageListener;
import pt.com.types.NetAction;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetParameter;
import pt.com.types.NetAction.DestinationType;

public abstract class BrokerListener implements MessageListener
{
	protected static NetMessage buildNotification(InternalMessage msg, DestinationType dtype)
	{
		return buildNotification(msg, null, dtype);
	}

	protected static NetMessage buildNotification(InternalMessage msg, String subscriptionName, DestinationType dtype )
	{
		NetNotification notification = new NetNotification(msg.getDestination(), dtype, msg.getContent(), subscriptionName);

		NetAction action = new NetAction(NetAction.ActionType.NOTIFICATION);
		action.setNotificationMessage(notification);
		
		notification.getMessage().setMessageId(msg.getMessageId());

		List<NetParameter> params = new ArrayList<NetParameter>(4);
		params.add(new NetParameter("FROM", msg.getSourceApp()));
		params.add(new NetParameter("ACTION", "http://services.sapo.pt/broker/notification/" + msg.getMessageId()));

		NetMessage message = new NetMessage(action, params);

		return message;
	}

	public abstract int addConsumer(IoSession iosession);

	public abstract int removeSessionConsumer(IoSession iosession);
}
