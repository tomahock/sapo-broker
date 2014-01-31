package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPong;
import pt.com.broker.types.NetProtocolType;

public class PongTest extends GenericNetMessageNegativeTest
{
	public PongTest()
	{
		super("Unexpected Message - Pong");

		NetPong pong = new NetPong("abcdefghijklmnopqrstuvwxyz");
		NetAction action = new NetAction(ActionType.PONG);
		action.setPongMessage(pong);
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
