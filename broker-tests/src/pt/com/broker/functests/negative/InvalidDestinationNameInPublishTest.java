package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;

public class InvalidDestinationNameInPublishTest extends GenericNetMessageNegativeTest
{
	public InvalidDestinationNameInPublishTest()
	{
		super("Invalid destination name - Publish with '@'");

		NetBrokerMessage brokerMsg = new NetBrokerMessage("This is the payload".getBytes());
		NetPublish publish = new NetPublish("service@/system/foo", DestinationType.TOPIC, brokerMsg);
		NetAction action = new NetAction(ActionType.PUBLISH);
		action.setPublishMessage(publish);
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
