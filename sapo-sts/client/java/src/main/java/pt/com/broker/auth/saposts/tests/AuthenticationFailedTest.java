package pt.com.broker.auth.saposts.tests;

import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

public class AuthenticationFailedTest extends GenericNetMessageNegativeTest
{
	public AuthenticationFailedTest()
	{
		super("Authentication Failed");

		if (!skipTest())
		{
			NetAuthentication clientAuth = new NetAuthentication("ThisIsAnInvalidToken".getBytes(), "SapoSTS");

			NetAction action = new NetAction(ActionType.AUTH);
			action.setAuthenticationMessage(clientAuth);
			NetMessage message = new NetMessage(action);
			setMessage(message);

			setFaultCode("3101");
			setFaultMessage("Authentication failed");
			try
			{
				SslBrokerClient bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), "tcp://mycompany.com/test", getEncodingProtocolType());

				setBrokerClient(bk);
			}
			catch (Throwable t)
			{
				setReasonForFailure(t);
			}
		}
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
	}
}
