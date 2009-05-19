package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.authentication.AuthenticationCredentialsProviderFactory;
import pt.com.security.authentication.sapoSts.SapoSTSAuthenticationCredentialsProvider;
import pt.com.security.authentication.sapoSts.SapoSTSAuthenticationParamsProvider;
import pt.com.types.NetProtocolType;

public class AuthenticationTopicSslTopicNameSpecified extends GenericPubSubTest
{
	public AuthenticationTopicSslTopicNameSpecified(){
		this("PubSub - Authentication Topic name specified");
	}
	
	public AuthenticationTopicSslTopicNameSpecified(String testName)
	{
		super(testName);
		setDestinationName("/secret/foo");
		setSubscriptionName("/secret/foo");
		
		//TODO: save these params in configuration
		String keyStoreLocation = "[location]";
		String keystorePassword = "[password]";
		
		String stsLocation = "https://services.sapo.pt/sts/";
		String stsUsername = "xxxxxxxxxxxxx";
		String stsPassword = "xxxxxxxxxxxxx";
		
		SslBrokerClient bk = null;
		try
		{
			AuthenticationCredentialsProviderFactory.addProvider("SapoSTS", new SapoSTSAuthenticationCredentialsProvider());
			SapoSTSAuthenticationParamsProvider.Parameters parameters = new SapoSTSAuthenticationParamsProvider.Parameters(stsLocation);
			SapoSTSAuthenticationParamsProvider.setSTSParameters(parameters);
			
			bk = new SslBrokerClient("127.0.0.1", 3390, "tcp://mycompany.com/mysniffer", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
			
			ClientAuthInfo clientAuthInfo = new ClientAuthInfo(stsUsername, stsPassword);
			clientAuthInfo.setUserAuthenticationType("SapoSTS");

			ClientAuthInfo stsClientCredentials = AuthenticationCredentialsProviderFactory.getProvider("SapoSTS").getCredentials(clientAuthInfo);
			
			bk.setAuthenticationCredentials(stsClientCredentials);
			
			bk.authenticateClient();
			
			Sleep.time(1000);
		}
		catch (Throwable e)
		{
			super.setFailure(e);
		}
		setInfoConsumer(bk );
	}
	
	@Override
	public boolean skipTest()
	{
		return getEncodingProtocolType() == NetProtocolType.SOAP;
	}
}
