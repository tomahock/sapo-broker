package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest.TestClientInfo;

public class Multiple1NTopicRemote extends MultipleGenericPubSubTest
{
	public Multiple1NTopicRemote(){
		super("Topic - 1 producer N consumers remote");
	}
	
	@Override
	protected void addConsumers()
	{
		super.addConsumers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3423, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), 1);
			tci.numberOfExecutions = getInfoProducers().size();

			this.addInfoConsumer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}
