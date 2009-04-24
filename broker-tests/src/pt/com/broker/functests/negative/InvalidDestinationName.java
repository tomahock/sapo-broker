package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAction;
import pt.com.types.NetMessage;
import pt.com.types.NetSubscribe;
import pt.com.types.NetAction.ActionType;
import pt.com.types.NetAction.DestinationType;

public class InvalidDestinationName extends GenericNetMessageNegativeTest
{
	public InvalidDestinationName()
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
