package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;

/* TODO TEMP CHANGE brsantos */
//import pt.com.broker.auth.CredentialsProvider;
//import pt.com.broker.auth.jdbc.JdbcProvider;
/* TEMP CHANGE brsantos */
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
			SslBrokerClient bk = null;
			try
			{
				bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
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

				/* TODO TEMP CHANGE brsantos */
				/*CredentialsProvider cp = new JdbcProvider(username, password);

				bk.setCredentialsProvider(cp);

				bk.authenticateClient();
				*/
				/* TEMP CHANGE brsantos */

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