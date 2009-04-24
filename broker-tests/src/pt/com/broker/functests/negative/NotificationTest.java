package pt.com.broker.functests.negative;

import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetMessage;
import pt.com.types.NetNotification;
import pt.com.types.NetAction.ActionType;
import pt.com.types.NetAction.DestinationType;


public class NotificationTest extends pt.com.broker.functests.helpers.GenericNetMessageNegativeTest
{
	public NotificationTest()
	{
		super("Unexpected Message - Notification");
		
		NetBrokerMessage brokerMessage = new NetBrokerMessage("data".getBytes());
		NetNotification notification = new NetNotification("/topic/foo", DestinationType.TOPIC, brokerMessage, "/topic/.*");
		NetAction action = new NetAction(ActionType.NOTIFICATION);
		action.setNotificationMessage(notification);
		NetMessage message = new NetMessage(action);
		setMessage(message);
		
		setFaultCode("1202");
		setFaultMessage("Unexpected message type");
	}
}
