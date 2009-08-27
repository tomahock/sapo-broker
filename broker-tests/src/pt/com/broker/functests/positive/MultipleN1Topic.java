package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;

public class MultipleN1Topic extends MultipleGenericPubSubTest
{
	public MultipleN1Topic()
	{
		super("Topic - N producers 1 consumer");
	}

	@Override
	protected void addProducers()
	{
		super.addProducers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = null;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}
