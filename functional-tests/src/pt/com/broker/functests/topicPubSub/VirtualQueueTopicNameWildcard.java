package pt.com.broker.functests.topicPubSub;

import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;

public class VirtualQueueTopicNameWildcard extends MultipleGenericVirtualQueuePubSubTest
{
	public VirtualQueueTopicNameWildcard()
	{
		super("VirtualQueue - Topic name is a wildcard");
		setSubscriptionName("xpto@/topic/.*");
	}
}
