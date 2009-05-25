package pt.com.broker.functests.positive;

import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.functests.helpers.GenericPubSubTest;

public class SslTopicNameSpeficied extends GenericPubSubTest
{
	public SslTopicNameSpeficied(){
		this("PubSub - SSL Topic name specified");
	}
	
	public SslTopicNameSpeficied(String testName)
	{
		super(testName);
		//TODO: save these params in configuration
		String keyStoreLocation = "[keystore location]";
		String keystorePassword = "[password]";
		SslBrokerClient bk = null;
		try
		{
			bk = new SslBrokerClient("127.0.0.1", 3390, "tcp://mycompany.com/mysniffer", getEncodingProtocolType(), keyStoreLocation, keystorePassword.toCharArray());
		}
		catch (Throwable e)
		{
			super.setFailure(e);
		}
		setInfoConsumer(bk );
	}
	
}