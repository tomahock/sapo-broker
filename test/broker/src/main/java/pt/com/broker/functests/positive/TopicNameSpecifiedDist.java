package pt.com.broker.functests.positive;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;

public class TopicNameSpecifiedDist extends TopicNameSpecified
{
	public TopicNameSpecifiedDist()
	{
		this("PubSub - Topic name specified with distant consumer");
	}

	public TopicNameSpecifiedDist(String testName)
	{
		super(testName);
		try
		{
            BrokerClient client = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(),  getEncodingProtocolType());

            client.connect();

			setInfoConsumer(client);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
