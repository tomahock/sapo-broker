package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetAction.ActionType;

public class UnknownAuthTypeFailedTest extends GenericNetMessageNegativeTest
{
	public UnknownAuthTypeFailedTest()
	{
		super("Unknown Authentication Type Failed");

		NetAuthentication clientAuth = new NetAuthentication("password".getBytes());
		clientAuth.setUserId("username");
		
		clientAuth.setAuthenticationType("BadAuthType");
		
		NetAction action = new NetAction(ActionType.AUTH);
		action.setAuthenticationMessage(clientAuth);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("3102");
		setFaultMessage("Unknown authentication type");
	}

	@Override
	public boolean skipTest()
	{
		return getEncodingProtocolType() == NetProtocolType.SOAP;
	}
}
