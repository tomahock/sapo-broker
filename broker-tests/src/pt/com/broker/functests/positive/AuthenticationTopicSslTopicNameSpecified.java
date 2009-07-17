package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProviderFactory;
import pt.com.broker.auth.saposts.SapoSTSParameterProvider;
import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

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
		String keyStoreLocation = "/home/lcosta/Work/SSL/clientKeystore";
		String keystorePassword = "changeit";
		
		String stsLocation = "https://services.sapo.pt/sts/";
		String stsUsername = "jose.saramago.276537@gmail.com";
		String stsPassword = "saramago_jose";
		
		SslBrokerClient bk = null;
		try
		{
			CredentialsProviderFactory.addProvider("SapoSTS", new SapoSTSProvider());
			SapoSTSParameterProvider.Parameters parameters = new SapoSTSParameterProvider.Parameters(stsLocation);
			SapoSTSParameterProvider.setSTSParameters(parameters);
			
			bk = new SslBrokerClient("127.0.0.1", 3390, "tcp://mycompany.com/mysniffer", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
			
			AuthInfo clientAuthInfo = new AuthInfo(stsUsername, stsPassword);
			clientAuthInfo.setUserAuthenticationType("SapoSTS");

			AuthInfo stsClientCredentials = CredentialsProviderFactory.getProvider("SapoSTS").getCredentials(clientAuthInfo);
			
			bk.setAuthenticationCredentials(stsClientCredentials);
			
			bk.authenticateClient();
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
