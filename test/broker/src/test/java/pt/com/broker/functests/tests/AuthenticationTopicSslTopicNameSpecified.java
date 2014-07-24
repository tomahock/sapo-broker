package pt.com.broker.functests.tests;

import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

public class AuthenticationTopicSslTopicNameSpecified extends GenericPubSubTest
{

    public AuthenticationTopicSslTopicNameSpecified(NetProtocolType protocolType) {
        super(protocolType);

        setName("PubSub - Authentication Topic name specified");

		setTimeout(2000);

		setDestinationName("/secret/foo");
		setSubscriptionName("/secret/foo");

		if (!skipTest())
		{
			String username = ConfigurationInfo.getParameter("sapoSts", "username");
			String password = ConfigurationInfo.getParameter("sapoSts", "password");

			CredentialsProvider cp = new SapoSTSProvider(username, password);

			SslBrokerClient bk = null;
			try
			{
				bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), getEncodingProtocolType());

				bk.setCredentialsProvider(cp);

				boolean authenticateClient = bk.authenticateClient();
				System.out.println("Client authenticated: " + authenticateClient);
			}
			catch (Throwable e)
			{
				super.setFailure(e);
			}
			setInfoConsumer(bk);
		}
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
	}
}