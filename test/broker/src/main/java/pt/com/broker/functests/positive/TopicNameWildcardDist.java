package pt.com.broker.functests.positive;

import pt.com.broker.client.nio.BrokerClient;
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
            BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent2-host"), BrokerTest.getAgent2Port(), getEncodingProtocolType());

            bk.connect();

			setInfoConsumer(bk);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
