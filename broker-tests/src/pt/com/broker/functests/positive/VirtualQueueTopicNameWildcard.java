package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;

public class VirtualQueueTopicNameWildcard extends MultipleGenericVirtualQueuePubSubTest
{
	public VirtualQueueTopicNameWildcard()
	{
		super("VirtualQueue - Topic name is a wildcard");
		setSubscriptionName(String.format("xpto@/%s/.*", getBaseName()));
	}
}
