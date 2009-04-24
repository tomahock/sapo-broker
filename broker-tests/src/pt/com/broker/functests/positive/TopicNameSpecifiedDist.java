package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;

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
			setInfoConsumer(new BrokerClient("127.0.0.1", 3423, "tcp://mycompany.com/mypublisher"));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
