package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;
import pt.com.broker.functests.helpers.MultipleNotificationsBrokerListener;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetProtocolType;

public class Multiple1NTopic extends MultipleGenericPubSubTest
{


    public Multiple1NTopic(NetProtocolType protocolType) {
        super(protocolType);
        setName("Topic - 1 producer N consumers");
    }

    @Override
	protected void addConsumers()
	{
		super.addConsumers();
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(getAgent1Hostname(), getAgent1Port(), getEncodingProtocolType());
            tci.brokerClient.connect();

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
