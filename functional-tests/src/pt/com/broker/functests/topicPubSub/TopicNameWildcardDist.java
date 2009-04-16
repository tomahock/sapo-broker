package pt.com.broker.functests.topicPubSub;

import pt.com.broker.client.BrokerClient;

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
			setInfoConsumer(new BrokerClient("127.0.0.1", 3423, "tcp://mycompany.com/mypublisher"));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
