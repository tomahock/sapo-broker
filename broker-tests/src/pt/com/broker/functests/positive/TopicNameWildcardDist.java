package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;

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
			setInfoConsumer(new BrokerClient(ConfigurationInfo.getParameter("agent2-host"), 
					Integer.parseInt(ConfigurationInfo.getParameter("agent2-port")), "tcp://mycompany.com/test"));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
