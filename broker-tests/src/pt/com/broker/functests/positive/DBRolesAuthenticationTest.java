package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

public class DBRolesAuthenticationTest extends GenericPubSubTest
{
	public DBRolesAuthenticationTest(){
		this("PubSub - Database authentication");
	}
	
	public DBRolesAuthenticationTest(String testName)
	{
		super(testName);
		setDestinationName("/secret/foo");
		setSubscriptionName("/secret/foo");
		
		//TODO: save these params in configuration
		String keyStoreLocation = "[location]";
		String keystorePassword = "[password]";
		
		String username = "username";
		String password = "password";
		
		SslBrokerClient bk = null;
		try
		{
						
			bk = new SslBrokerClient("127.0.0.1", 3390, "tcp://mycompany.com/mysniffer", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
			
			AuthInfo clientAuthInfo = new AuthInfo(username, password);
			clientAuthInfo.setUserAuthenticationType("BrokerRolesDB");

			bk.setAuthenticationCredentials(clientAuthInfo);
			
			bk.authenticateClient();
			
			Sleep.time(1000);
		}
		catch (Throwable e)
		{
			super.setFailure(e);
			return;
		}
		setInfoConsumer(bk );
	}
	
	@Override
	public boolean skipTest()
	{
		return getEncodingProtocolType() == NetProtocolType.SOAP;
	}
}