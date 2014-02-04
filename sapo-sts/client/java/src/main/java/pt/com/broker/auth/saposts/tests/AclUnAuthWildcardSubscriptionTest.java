package pt.com.broker.auth.saposts.tests;

import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericBrokerListener;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

public class AclUnAuthWildcardSubscriptionTest extends GenericPubSubTest
{
	public AclUnAuthWildcardSubscriptionTest() throws Exception
	{
		this("Acl PubSub - Unauthenticated  Wilcard");
	}

	public AclUnAuthWildcardSubscriptionTest(String testName) throws Exception
	{
		super(testName);

		String username = ConfigurationInfo.getParameter("sapoSts", "username");
		String password = ConfigurationInfo.getParameter("sapoSts", "password");

		CredentialsProvider cp = new SapoSTSProvider(username, password);

		setDestinationName("/app/test/private");
		setSubscriptionName("/app/.*");

		setTimeout(12 * 1000);

		if (!skipTest())
		{
			SslBrokerClient bk_producer = null;
			try
			{

				bk_producer = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), "tcp://mycompany.com/test", getEncodingProtocolType());

				bk_producer.setCredentialsProvider(cp);

				if (!bk_producer.authenticateClient())
				{
					System.out.println("AUTH FAILED");

					this.constructionFailed = true;
					this.reasonForFailure = new RuntimeException("Client Authentication failed");
				}
			}
			catch (Throwable e)
			{
				super.setFailure(e);
			}

			setInfoProducer(bk_producer);

			// Not authenticated - So, it shouldn't receive a message.
			setOkToTimeOut(true);

			this.setBrokerListener(new GenericBrokerListener(DestinationType.TOPIC)
			{
				@Override
				public void onMessage(NetNotification message)
				{
					System.out.println("ERROR: Should not receive message");

					setFailure(new RuntimeException("Message received. No message should be received."));

					setOkToTimeOut(false);
				}
			});
		}
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
	}
}
