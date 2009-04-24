package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAction;
import pt.com.types.NetFault;
import pt.com.types.NetMessage;
import pt.com.types.NetAction.ActionType;

public class FaultWithActionIdTest extends GenericNetMessageNegativeTest
{
	public FaultWithActionIdTest()
	{
		super("Unexpected Message - Fault");

		String actionId = "THISISANACTIONID";
		
		NetFault fault = new NetFault("1234", "This sould fail");
		fault.setActionId(actionId);
		NetAction action = new NetAction(ActionType.FAULT);
		action.setFaultMessage(fault);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("1202");
		setFaultMessage("Unexpected message type");
		setFaultActionId(actionId);
	}
}
