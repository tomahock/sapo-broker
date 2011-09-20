package pt.com.broker.functests.negative;

import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

public class InvalidAuthChannelTypeTest extends GenericNetMessageNegativeTest
{
	public InvalidAuthChannelTypeTest()
	{
		super("Invalid Authentication Channel Type Failed");

		NetAuthentication clientAuth = new NetAuthentication("ThisIsAnInvalidTokenNotThatItMatters".getBytes(), "AuthType");

		NetAction action = new NetAction(ActionType.AUTH);
		action.setAuthenticationMessage(clientAuth);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("3103");
		setFaultMessage("Invalid authentication channel type");
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON);
	}
}
