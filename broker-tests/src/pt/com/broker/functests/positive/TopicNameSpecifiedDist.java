package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;

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
			setInfoConsumer(new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), 
					Integer.parseInt(ConfigurationInfo.getParameter("agent1-port")), "tcp://mycompany.com/test"));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
