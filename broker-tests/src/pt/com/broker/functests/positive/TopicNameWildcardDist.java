package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;

public class TopicNameWildcardDist extends TopicNameWildcard
{
	public TopicNameWildcardDist()
	{
		this("PubSub - Topic name is a wildcard with distant consumer");
	}

	public TopicNameWildcardDist(String testName)
	{
		super(testName);
		try
		{
			int port = BrokerTest.getAgent2Port();
			
			System.out.println("TopicNameWildcardDist.TopicNameWildcardDist(). port: "+ port);
			
			System.out.println("TopicNameWildcardDist.TopicNameWildcardDist(). host: "+ ConfigurationInfo.getParameter("agent2-host"));
			
			setInfoConsumer(new BrokerClient(ConfigurationInfo.getParameter("agent2-host"), port, "tcp://mycompany.com/test", getEncodingProtocolType()));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
