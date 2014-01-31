package pt.com.broker.functests.helpers;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetAction.DestinationType;

public class MultipleGenericQueuePubSubTest extends MultipleGenericPubSubTest
{
	private int consumerNotifications = 0;

	public MultipleGenericQueuePubSubTest()
	{
		this("MultipleGenericQueuePubSubTest");
	}

	public MultipleGenericQueuePubSubTest(String testName)
	{
		super(testName);
		setDestinationType(DestinationType.QUEUE);
		setConsumerDestinationType(DestinationType.QUEUE);
	}

	protected void addConsumers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), getConsumerNotifications());
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

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
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

}
