package pt.com.broker.functests.topicPubSub;

import pt.com.broker.functests.helpers.GenericPubSubTest;

public class TopicNameWildcard extends GenericPubSubTest
{
	public TopicNameWildcard()
	{
		this("PubSub - Topic name is a wildcard");
	}
	
	public TopicNameWildcard(String testName)
	{
		super(testName);
		setSubscriptionName("/topic/.*");	
	}
}
