package pt.com.broker.functests.positive;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;
import pt.com.broker.types.NetProtocolType;

public class MultipleNNTopicRemote extends MultipleGenericPubSubTest
{

	public MultipleNNTopicRemote(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Topic - N producer N consumers remote");
	}

	@Override
	protected void addConsumers()
	{
		super.addConsumers();
		try
		{
			int numberOfExecutions = getInfoProducers().size();

			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(getAgent2Hostname(), getAgent2Port(), getEncodingProtocolType());
			tci.brokerClient.connect();

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

			tci.brokerClient = new BrokerClient(getAgent2Hostname(), getAgent2Port(), getEncodingProtocolType());
			tci.brokerClient.connect();

			tci.brokerListenter = null;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			this.setFailure(t);
		}
	}
}
