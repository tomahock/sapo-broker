package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.GenericPubSubTest;

public class TopicNameSpecified extends GenericPubSubTest
{
	public TopicNameSpecified()
	{
		this("PubSub - Topic name specified");
	}

	public TopicNameSpecified(String testName)
	{
		super(testName);
	}

}
