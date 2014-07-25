package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.*;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;

public class EmptyDestinationNameInPublication extends GenericNetMessageNegativeTest
{

    public EmptyDestinationNameInPublication(NetProtocolType protocolType) {
        super(protocolType);

        setName("Empty destination name in publication");


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
