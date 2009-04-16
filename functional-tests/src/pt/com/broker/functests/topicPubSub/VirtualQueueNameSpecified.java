package pt.com.broker.functests.topicPubSub;

import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;

public class VirtualQueueNameSpecified extends MultipleGenericVirtualQueuePubSubTest
{
	public VirtualQueueNameSpecified()
	{
		super("VirtualQueue - Topic name specified");
	}
}
