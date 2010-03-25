package pt.com.broker.functests.negative;

import org.caudexorigo.cli.CliRuntimeException;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
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
		clientAuth.setAuthenticationType("SapoSTS");
		
		NetAction action = new NetAction(ActionType.AUTH);
		action.setAuthenticationMessage(clientAuth);
		NetMessage message = new NetMessage(action);
		setMessage(message);

		setFaultCode("3101");
		setFaultMessage("Authentication failed");
		try
		{
			String keyStoreLocation = ConfigurationInfo.getParameter("keystoreLocation");
			String keystorePassword = ConfigurationInfo.getParameter("keystorePassword");
			
			SslBrokerClient bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), 
					Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), "tcp://mycompany.com/test", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
			
			setBrokerClient(bk);
		}
		catch (Throwable t)
		{
			setReasonForFailure(t);
		}

	}

	@Override
	public boolean skipTest()
	{
		return getEncodingProtocolType() == NetProtocolType.SOAP;
	}
}
