package pt.com.broker.functests.topicPubSub;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericQueuePubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class MultipleNNQueue extends MultipleGenericQueuePubSubTest
{
	public MultipleNNQueue(){
		super("Queue - N producer N consumers");
	}
	
	@Override
	protected void addConsumers()
	{
		setConsumerNotifications(1);
		
		super.addConsumers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), getConsumerNotifications());
			tci.numberOfExecutions = getConsumerNotifications();

			this.addInfoConsumer(tci);
			
			System.out.println("getConsumerNotifications() = " + getConsumerNotifications());
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
