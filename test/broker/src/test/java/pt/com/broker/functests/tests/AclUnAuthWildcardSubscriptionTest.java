package pt.com.broker.functests.tests;

import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

public class AclUnAuthWildcardSubscriptionTest extends GenericPubSubTest
{
	public AclUnAuthWildcardSubscriptionTest(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Acl PubSub - Unauthenticated  Wilcard");

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

				bk_producer = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), getEncodingProtocolType());

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

			this.setBrokerListener(new NotificationListenerAdapter()
			{
				@Override
				public boolean onMessage(NetNotification message, HostInfo host)
				{
					System.out.println("ERROR: Should not receive message");

					setFailure(new RuntimeException("Message received. No message should be received."));

					setOkToTimeOut(false);

					return true;
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
