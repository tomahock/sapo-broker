package pt.com.broker.functests.negative;

import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
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

		if (!skipTest())
		{
			NetAuthentication clientAuth = new NetAuthentication("password".getBytes(), "BadAuthType");
			clientAuth.setUserId("username");

			NetAction action = new NetAction(ActionType.AUTH);
			action.setAuthenticationMessage(clientAuth);
			NetMessage message = new NetMessage(action);
			setMessage(message);

			setFaultCode("3102");
			setFaultMessage("Unknown authentication type");
			SslBrokerClient bk = null;
			try
			{
				String keyStoreLocation = ConfigurationInfo.getParameter("keystoreLocation");
				String keystorePassword = ConfigurationInfo.getParameter("keystorePassword");

				bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), "tcp://mycompany.com/test", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());

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
