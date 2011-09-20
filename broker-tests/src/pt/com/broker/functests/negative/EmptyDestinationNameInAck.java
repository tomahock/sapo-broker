package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAcknowledge;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

public class EmptyDestinationNameInAck extends GenericNetMessageNegativeTest
{
	public EmptyDestinationNameInAck()
	{
		super("Empty destination name in ack");

		NetAcknowledge netAcknowledge = new NetAcknowledge("", "This is a fake action id.");
		NetAction action = new NetAction(ActionType.ACKNOWLEDGE);
		action.setAcknowledgeMessage(netAcknowledge);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("2001");
		setFaultMessage("Invalid destination name");
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
