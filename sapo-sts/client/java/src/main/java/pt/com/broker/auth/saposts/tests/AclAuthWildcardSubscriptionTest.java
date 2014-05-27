package pt.com.broker.auth.saposts.tests;

import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

public class AclAuthWildcardSubscriptionTest extends GenericPubSubTest
{
	public AclAuthWildcardSubscriptionTest() throws Exception
	{
		this("Acl PubSub - Unauthenticated  Wilcard");
	}

	public AclAuthWildcardSubscriptionTest(String testName) throws Exception
	{
		super(testName);

		String username = ConfigurationInfo.getParameter("sapoSts", "username");
		String password = ConfigurationInfo.getParameter("sapoSts", "password");

		CredentialsProvider cp = new SapoSTSProvider(username, password);

		setDestinationName("/app/test/private");
		setSubscriptionName("/app/.*");

		if (!skipTest())
		{
			SslBrokerClient bk_producer = null;
			SslBrokerClient bk_consumer = null;

			setTimeout(20 * 1000);

			try
			{
				bk_producer = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), getEncodingProtocolType());

				bk_producer.setCredentialsProvider(cp);

				if (!bk_producer.authenticateClient())
				{
					System.out.println("AUTH FAILED");

					this.setFailure(new RuntimeException("Client Authentication failed"));
				}

				bk_consumer = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), getEncodingProtocolType());

				bk_consumer.setCredentialsProvider(cp);

				if (!bk_consumer.authenticateClient())
				{
					System.out.println("AUTH FAILED 2");

					this.setFailure(new RuntimeException("Client Authentication failed"));
				}
			}
			catch (Throwable e)
			{
				super.setFailure(e);
			}

			setInfoProducer(bk_producer);

			setInfoConsumer(bk_consumer);
		}
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
	}
}
