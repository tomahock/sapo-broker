package pt.com.broker.functests.negative;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

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

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
