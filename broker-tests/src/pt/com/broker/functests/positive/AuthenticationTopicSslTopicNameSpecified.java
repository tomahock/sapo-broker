//package pt.com.broker.functests.positive;
//
//import pt.com.broker.auth.AuthInfo;
//import pt.com.broker.auth.CredentialsProviderFactory;
//import pt.com.broker.auth.saposts.SapoSTSParameterProvider;
//import pt.com.broker.auth.saposts.SapoSTSProvider;
//import pt.com.broker.client.SslBrokerClient;
//import pt.com.broker.functests.conf.ConfigurationInfo;
//import pt.com.broker.functests.helpers.GenericPubSubTest;
//import pt.com.broker.types.NetProtocolType;
//
//public class AuthenticationTopicSslTopicNameSpecified extends GenericPubSubTest
//{
//	public AuthenticationTopicSslTopicNameSpecified()
//	{
//		this("PubSub - Authentication Topic name specified");
//		setTimeout(2000);
//	}
//
//	public AuthenticationTopicSslTopicNameSpecified(String testName)
//	{
//		super(testName);
//		setDestinationName("/secret/foo");
//		setSubscriptionName("/secret/foo");
//
//		String keyStoreLocation = ConfigurationInfo.getParameter("keystoreLocation");
//		String keystorePassword = ConfigurationInfo.getParameter("keystorePassword");
//
//		String stsLocation = "https://services.sapo.pt/sts/";
//		String stsUsername = ConfigurationInfo.getParameter("sapoSts", "username");
//		String stsPassword = ConfigurationInfo.getParameter("sapoSts", "password");
//
//		SslBrokerClient bk = null;
//		try
//		{
//			CredentialsProviderFactory.addProvider("SapoSTS", new SapoSTSProvider());
//			SapoSTSParameterProvider.Parameters parameters = new SapoSTSParameterProvider.Parameters(stsLocation);
//			SapoSTSParameterProvider.setSTSParameters(parameters);
//
//			bk = new SslBrokerClient(ConfigurationInfo.getParameter("agent1-host"), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), "tcp://mycompany.com/test", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
//
//			AuthInfo clientAuthInfo = new AuthInfo(stsUsername, stsPassword);
//			clientAuthInfo.setUserAuthenticationType("SapoSTS");
//
//			AuthInfo stsClientCredentials = CredentialsProviderFactory.getProvider("SapoSTS").getCredentials(clientAuthInfo);
//
//			bk.setAuthenticationCredentials(stsClientCredentials);
//
//			bk.authenticateClient();
//		}
//		catch (Throwable e)
//		{
//			super.setFailure(e);
//		}
//		setInfoConsumer(bk);
//	}
//
//	@Override
//	public boolean skipTest()
//	{
//		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0);
//	}
//}
