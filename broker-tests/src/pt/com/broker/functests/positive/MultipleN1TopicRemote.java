package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;

public class MultipleN1TopicRemote extends MultipleGenericPubSubTest
{
	public MultipleN1TopicRemote(){
		super("Topic - N producers 1 consumer remote");
	}
	
	@Override
	protected void addProducers()
	{
		super.addProducers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3423, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = null;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}