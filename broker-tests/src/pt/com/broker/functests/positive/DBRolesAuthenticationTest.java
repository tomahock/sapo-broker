package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

public class DBRolesAuthenticationTest extends GenericPubSubTest
{
	public DBRolesAuthenticationTest()
	{
		this("PubSub - Database authentication");
	}

	public DBRolesAuthenticationTest(String testName)
	{
		super(testName);
		setDestinationName("/secret/foo");
		setSubscriptionName("/secret/foo");

		String keyStoreLocation = ConfigurationInfo.getParameter("keystoreLocation");
		String keystorePassword = ConfigurationInfo.getParameter("keystorePassword");

		SslBrokerClient bk = null;
		try
		{
			bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), 
						Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), "tcp://mycompany.com/test", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
		}
		catch (Throwable e)
		{
			super.setFailure(e);
			return;
		}
		setInfoConsumer(bk);
	}

	@Override
	protected void addPrerequisites()
	{
		String username = ConfigurationInfo.getParameter("jdbcTest", "username");
		String password = ConfigurationInfo.getParameter("jdbcTest", "password");

		try
		{
			SslBrokerClient bk = (SslBrokerClient) getInfoConsumer();

			AuthInfo clientAuthInfo = new AuthInfo(username, password);
			clientAuthInfo.setUserAuthenticationType("BrokerRolesDB");

			bk.setAuthenticationCredentials(clientAuthInfo);

			bk.authenticateClient();

			Sleep.time(1000);
		}
		catch (Throwable e)
		{
			super.setFailure(e);
			return;
		}

		super.addPrerequisites();
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
	}
}