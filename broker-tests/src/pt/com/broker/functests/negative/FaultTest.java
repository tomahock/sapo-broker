package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

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

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
