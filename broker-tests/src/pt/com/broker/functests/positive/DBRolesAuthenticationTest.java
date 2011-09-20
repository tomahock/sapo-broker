package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.jdbc.JdbcProvider;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
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

		if (!skipTest())
		{
			String keyStoreLocation = ConfigurationInfo.getParameter("keystoreLocation");
			String keystorePassword = ConfigurationInfo.getParameter("keystorePassword");
			SslBrokerClient bk = null;
			try
			{
				bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType(), keyStoreLocation, keystorePassword);
			}
			catch (Throwable e)
			{
				super.setFailure(e);
			}
			setInfoConsumer(bk);
		}
	}

	@Override
	protected void addPrerequisites()
	{
		if (!skipTest())
		{
			String username = ConfigurationInfo.getParameter("jdbcTest", "username");
			String password = ConfigurationInfo.getParameter("jdbcTest", "password");

			try
			{
				SslBrokerClient bk = (SslBrokerClient) getInfoConsumer();

				CredentialsProvider cp = new JdbcProvider(username, password);

				bk.setCredentialsProvider(cp);

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
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
	}
}