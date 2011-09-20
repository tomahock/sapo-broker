package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;

public class MultipleNNTopicRemote extends MultipleGenericPubSubTest
{
	public MultipleNNTopicRemote()
	{
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

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent2-host"), BrokerTest.getAgent2Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
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

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent2-host"), BrokerTest.getAgent2Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
			tci.brokerListenter = null;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}
