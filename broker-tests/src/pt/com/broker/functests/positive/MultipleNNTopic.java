package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class MultipleNNTopic extends MultipleGenericPubSubTest
{
	public MultipleNNTopic(){
		super("Topic - N producer N consumers");
	}
	
	@Override
	protected void addConsumers()
	{
		super.addConsumers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), 1);
			tci.numberOfExecutions = getInfoProducers().size();

			this.addInfoConsumer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
	
	@Override
	protected void addProducers()
	{
		super.addProducers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = null;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}

}
