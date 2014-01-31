package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetSubscribe;

public class InvalidDestinationNameTest extends GenericNetMessageNegativeTest
{
	public InvalidDestinationNameTest()
	{
		super("Invalid destination name");

		NetSubscribe subscribe = new NetSubscribe("/topic/foo", DestinationType.VIRTUAL_QUEUE);
		NetAction action = new NetAction(ActionType.SUBSCRIBE);
		action.setSubscribeMessage(subscribe);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("2001");
		setFaultMessage("Invalid destination name");
	}
}
