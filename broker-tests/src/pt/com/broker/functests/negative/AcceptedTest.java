package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAccepted;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

public class AcceptedTest extends GenericNetMessageNegativeTest
{
	public AcceptedTest()
	{
		super("Unexpected Message - Accepted");

		NetAccepted accepted = new NetAccepted("123456789-987654321");
		NetAction action = new NetAction(ActionType.ACCEPTED);
		action.setAcceptedMessage(accepted);
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
