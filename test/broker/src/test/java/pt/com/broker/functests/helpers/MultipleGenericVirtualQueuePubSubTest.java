package pt.com.broker.functests.helpers;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class MultipleGenericVirtualQueuePubSubTest extends MultipleGenericPubSubTest
{
	private int consumerNotifications = 0;


    public MultipleGenericVirtualQueuePubSubTest(NetProtocolType protocolType) {
        super(protocolType);

        setName("MultipleGenericVirtualQueuePubSubTest");

		setSubscriptionName("xpto@" + getSubscriptionName());
		setConsumerDestinationType(DestinationType.VIRTUAL_QUEUE);
	}

	protected void addConsumers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(getAgent1Hostname(), getAgent1Port() , getEncodingProtocolType());
            tci.brokerClient.connect();

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

			tci.brokerClient = new BrokerClient(getAgent1Hostname(), getAgent1Port(), getEncodingProtocolType());
            tci.brokerClient.connect();

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
