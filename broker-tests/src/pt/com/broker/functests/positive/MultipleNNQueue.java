package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.MultipleGenericQueuePubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class MultipleNNQueue extends MultipleGenericQueuePubSubTest
{
	public MultipleNNQueue()
	{
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

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), getConsumerNotifications());
			tci.numberOfExecutions = getConsumerNotifications();

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

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
			tci.brokerListenter = null;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}
