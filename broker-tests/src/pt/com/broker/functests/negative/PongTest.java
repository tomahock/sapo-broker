package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAction;
import pt.com.types.NetMessage;
import pt.com.types.NetPong;
import pt.com.types.NetAction.ActionType;

public class PongTest extends GenericNetMessageNegativeTest
{
	public PongTest()
	{
		super("Unexpected Message - Pong");

		NetPong pong = new NetPong(System.currentTimeMillis());
		NetAction action = new NetAction(ActionType.PONG);
		action.setPongMessage(pong);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("1202");
		setFaultMessage("Unexpected message type");
	}
}
