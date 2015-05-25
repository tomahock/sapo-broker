package pt.com.broker.functests.positive;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

public class MultipleN1TopicRemote extends MultipleGenericPubSubTest
{

	public MultipleN1TopicRemote(NetProtocolType protocolType)
	{
		super(protocolType);
		setName("Topic - N producers 1 consumer remote");
	}

	@Override
	protected void addProducers()
	{
		super.addProducers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(getAgent2Hostname(), getAgent2Port(), getEncodingProtocolType());
			tci.brokerClient.connect();

			tci.brokerListenter = null;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}