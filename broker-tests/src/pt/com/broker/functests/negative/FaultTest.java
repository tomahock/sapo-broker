package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAction;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetAction.ActionType;

public class FaultTest extends GenericNetMessageNegativeTest
{
	public FaultTest()
	{
		super("Unexpected Message - Fault");

		NetFault fault = new NetFault("1234", "This sould fail");
		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(fault);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("1202");
		setFaultMessage("Unexpected message type");
	}
}
