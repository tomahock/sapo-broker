package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetProtocolType;

public class VirtualQueueNameSpecifiedRemote extends MultipleGenericVirtualQueuePubSubTest
{

    public VirtualQueueNameSpecifiedRemote(NetProtocolType protocolType) {
        super(protocolType);
        setName("VirtualQueue Remote- Topic name specified");
    }



	protected void addConsumers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(getAgent2Hostname(), getAgent2Port(), getEncodingProtocolType());
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

}
