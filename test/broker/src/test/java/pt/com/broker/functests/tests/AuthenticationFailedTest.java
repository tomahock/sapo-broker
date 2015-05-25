package pt.com.broker.functests.tests;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericNetMessageNegativeTest;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAuthentication;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;

public class AuthenticationFailedTest extends GenericNetMessageNegativeTest
{

	private static final Logger log = LoggerFactory.getLogger(AuthenticationFailedTest.class);

	public AuthenticationFailedTest(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Authentication Failed");

		if (!skipTest())
		{
			NetAuthentication clientAuth = new NetAuthentication("ThisIsAnInvalidToken".getBytes(), "SapoSTS");

			NetAction action = new NetAction(ActionType.AUTH);
			action.setAuthenticationMessage(clientAuth);
			NetMessage message = new NetMessage(action);
			setMessage(message);

			setFaultCode("3101");
			setFaultMessage("Authentication failed");
			try
			{
				SslBrokerClient bk = getBrokerClient();
				setBrokerClient(bk);
			}
			catch (Throwable t)
			{
				setReasonForFailure(t);
			}
		}
	}

	@Override
	public SslBrokerClient getBrokerClient()
	{
		try
		{
			log.debug("Setting up SSL Settings.");
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(getClass().getClassLoader().getResourceAsStream("mykeystore.jks"), "password".toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keyStore, "jordan".toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SslBrokerClient client = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), getEncodingProtocolType());
			client.setContext(sslContext);
			client.connect();
			return client;
		}
		catch (Exception e)
		{
			log.error("Exception caught instantiating broker client.", e);
		}
		return null;
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
	}
}
