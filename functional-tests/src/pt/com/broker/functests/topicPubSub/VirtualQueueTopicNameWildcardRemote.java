package pt.com.broker.functests.topicPubSub;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class VirtualQueueTopicNameWildcardRemote extends MultipleGenericVirtualQueuePubSubTest
{
	public VirtualQueueTopicNameWildcardRemote()
	{
		super("VirtualQueue Remote- Topic name wildcard");
		setSubscriptionName("xpto@/topic/.*");
	}
	
	protected void addConsumers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3423, "tcp://mycompany.com/mypublisher");
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getConsumerDestinationType(), getConsumerNotifications());
			tci.numberOfExecutions = getConsumerNotifications();

			this.addInfoConsumer(tci);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}

	}
}
