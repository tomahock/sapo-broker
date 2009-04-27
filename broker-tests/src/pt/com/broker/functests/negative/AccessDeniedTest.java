package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetMessage;
import pt.com.types.NetPublish;
import pt.com.types.NetAction.ActionType;
import pt.com.types.NetAction.DestinationType;

public class AccessDeniedTest extends GenericNetMessageNegativeTest
{
	public AccessDeniedTest()
	{
		super("Access Denied");
		
		NetBrokerMessage brokerMsg = new NetBrokerMessage("This is the payload".getBytes());
		NetPublish publish = new NetPublish("/system/foo", DestinationType.TOPIC, brokerMsg);
		NetAction action = new NetAction(ActionType.PUBLISH);
		action.setPublishMessage(publish);		
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("3201");
		setFaultMessage("Access denied");
	}
}
