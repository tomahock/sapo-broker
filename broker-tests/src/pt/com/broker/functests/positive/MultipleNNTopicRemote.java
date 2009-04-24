package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class MultipleNNTopicRemote  extends MultipleGenericPubSubTest
{
	public MultipleNNTopicRemote(){
		super("Topic - N producer N consumers remote");
	}
	
	@Override
	protected void addConsumers()
	{
		super.addConsumers();
		try
		{
			int numberOfExecutions = getInfoProducers().size();
			
			TestClientInfo tci = new TestClientInfo();

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3423, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), numberOfExecutions);
			tci.numberOfExecutions = numberOfExecutions;

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
