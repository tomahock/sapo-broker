package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.NetProtocolType;

public class EmptyDestinationNameInPoll extends GenericNetMessageNegativeTest
{
	public EmptyDestinationNameInPoll()
	{
		super("Empty destination name in poll");

		NetPoll poll = new NetPoll("", 120 * 1000);

		NetAction action = new NetAction(ActionType.POLL);
		action.setPollMessage(poll);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("2001");
		setFaultMessage("Invalid destination name");
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
