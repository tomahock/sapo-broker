package pt.com.broker.functests.helpers;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetAction.DestinationType;

public class MultipleGenericVirtualQueuePubSubTest extends MultipleGenericPubSubTest
{
	private int consumerNotifications = 0;

	public MultipleGenericVirtualQueuePubSubTest()
	{
		this("MultipleGenericVirtualQueuePubSubTest");
	}

	public MultipleGenericVirtualQueuePubSubTest(String testName)
	{
		super(testName);
		setSubscriptionName("xpto@" + getSubscriptionName());
		setConsumerDestinationType(DestinationType.VIRTUAL_QUEUE);
	}

	protected void addConsumers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), 
					Integer.parseInt(ConfigurationInfo.getParameter("agent1-port")), "tcp://mycompany.com/test");
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getConsumerDestinationType(), getConsumerNotifications());
			tci.numberOfExecutions = getConsumerNotifications();

			this.addInfoConsumer(tci);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}

	}

	protected void addProducers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), 
					Integer.parseInt(ConfigurationInfo.getParameter("agent1-port")), "tcp://mycompany.com/test");
			tci.brokerListenter = null;
			tci.numberOfExecutions = 1;

			this.addInfoProducer(tci);

			setConsumerNotifications(getInfoProducers().size());
		}
		catch (Throwable t)
		{
			setFailure(t);
		}

	}

	public void setConsumerNotifications(int consumerNotifications)
	{
		this.consumerNotifications = consumerNotifications;
	}

	public int getConsumerNotifications()
	{
		return consumerNotifications;
	}

	public DestinationType getConsumerDestinationType()
	{
		return DestinationType.VIRTUAL_QUEUE;
	}
}
