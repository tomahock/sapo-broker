package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAccepted;
import pt.com.types.NetAction;
import pt.com.types.NetMessage;
import pt.com.types.NetAction.ActionType;

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
}
