package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAction;
import pt.com.types.NetAuthentication;
import pt.com.types.NetMessage;
import pt.com.types.NetProtocolType;
import pt.com.types.NetAction.ActionType;

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
