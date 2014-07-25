package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.GenericPubSubTest;
import org.caudexorigo.concurrent.Sleep;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetProtocolType;

/* TODO TEMP CHANGE brsantos */
//import pt.com.broker.auth.CredentialsProvider;
//import pt.com.broker.auth.jdbc.JdbcProvider;
/* TEMP CHANGE brsantos */

public class DBRolesAuthenticationTest extends GenericPubSubTest
{

    public DBRolesAuthenticationTest(NetProtocolType protocolType) {
        super(protocolType);


        setName("PubSub - Database authentication");

		setDestinationName("/secret/foo");
		setSubscriptionName("/secret/foo");

		if (!skipTest())
		{
			SslBrokerClient bk = null;
			try
			{
				bk = new SslBrokerClient(getAgent1Hostname(), getAgent1Port(), getEncodingProtocolType());
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