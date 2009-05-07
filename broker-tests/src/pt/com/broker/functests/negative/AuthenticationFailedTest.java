package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.types.NetAction;
import pt.com.types.NetAuthentication;
import pt.com.types.NetMessage;
import pt.com.types.NetProtocolType;
import pt.com.types.NetAction.ActionType;
import pt.com.types.NetAuthentication.AuthMessageType;

public class AuthenticationFailedTest extends GenericNetMessageNegativeTest
{
	public AuthenticationFailedTest()
	{
		super("Authentication Failed");
		
		NetAuthentication.AuthClientAuthentication clientAuth = new NetAuthentication.AuthClientAuthentication("ThisIsAnInvalidToken".getBytes(), "123456789");
		NetAuthentication auth = new NetAuthentication(AuthMessageType.CLIENT_AUTH);
		auth.setAuthClientAuthentication(clientAuth);
		
 		NetAction action = new NetAction(ActionType.AUTH);
		action.setAuthenticationMessage(auth);		
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("3101");
		setFaultMessage("Authentication failed");
	}
	@Override
	public boolean skipTest()
	{
		return getEncodingProtocolType() != NetProtocolType.PROTOCOL_BUFFER;
	}
}
