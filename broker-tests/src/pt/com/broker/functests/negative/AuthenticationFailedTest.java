package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetAction.ActionType;

public class AuthenticationFailedTest extends GenericNetMessageNegativeTest
{
	public AuthenticationFailedTest()
	{
		super("Authentication Failed");

		NetAuthentication clientAuth = new NetAuthentication("ThisIsAnInvalidToken".getBytes());

		NetAction action = new NetAction(ActionType.AUTH);
		action.setAuthenticationMessage(clientAuth);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("3101");
		setFaultMessage("Authentication failed");
	}

	@Override
	public boolean skipTest()
	{
		return getEncodingProtocolType() == NetProtocolType.SOAP;
	}
}
