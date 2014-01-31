package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;

public class EmptyDestinationNameInPublication extends GenericNetMessageNegativeTest
{
	public EmptyDestinationNameInPublication()
	{
		super("Empty destination name in publication");

		NetPublish publish = new NetPublish("", DestinationType.TOPIC, new NetBrokerMessage("content"));
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
