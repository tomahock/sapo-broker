package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.MultipleGenericPubSubTest;

public class MultipleN1TopicRemote extends MultipleGenericPubSubTest
{
	public MultipleN1TopicRemote()
	{
		super("Topic - N producers 1 consumer remote");
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