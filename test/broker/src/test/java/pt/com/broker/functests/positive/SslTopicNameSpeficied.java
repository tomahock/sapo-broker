package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetProtocolType;

public class SslTopicNameSpeficied extends GenericPubSubTest
{

    public SslTopicNameSpeficied(NetProtocolType protocolType) {
        super(protocolType);

        setName("PubSub - SSL Topic name specified");
		if (!skipTest())
		{
			SslBrokerClient bk = null;
			try
			{
				bk = new SslBrokerClient(getAgent1Hostname(), Integer.parseInt(ConfigurationInfo.getParameter("agent1-ssl-port")), getEncodingProtocolType());
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