package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;

public class InvalidDestinationType extends GenericNetMessageNegativeTest
{
	public InvalidDestinationType()
	{
		super("Invalid destination type - VirtualQueue");

		NetBrokerMessage brokerMsg = new NetBrokerMessage("This is the payload".getBytes());
		NetPublish publish = new NetPublish("/topic/foo", DestinationType.VIRTUAL_QUEUE, brokerMsg);
		NetAction action = new NetAction(ActionType.PUBLISH);
		action.setPublishMessage(publish);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("2002");
		setFaultMessage("Invalid destination type");
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON); // SOAP codec throws exception while encondig message. The others don't.
	}
}
