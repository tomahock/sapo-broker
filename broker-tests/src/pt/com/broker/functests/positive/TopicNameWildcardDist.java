package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;

public class TopicNameWildcardDist extends TopicNameWildcard
{
	public TopicNameWildcardDist()
	{
		this("PubSub - Topic name is a wildcard with remote consumer");
	}

	public TopicNameWildcardDist(String testName)
	{
		super(testName);
		try
		{
			setInfoConsumer(new BrokerClient(ConfigurationInfo.getParameter("agent2-host"), BrokerTest.getAgent2Port(), "tcp://mycompany.com/test", getEncodingProtocolType()));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
