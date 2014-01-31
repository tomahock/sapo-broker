package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
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
			setInfoConsumer(new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType()));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
