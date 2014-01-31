package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;

public class VirtualQueueNameSpecified extends MultipleGenericVirtualQueuePubSubTest
{
	public VirtualQueueNameSpecified()
	{
		super("VirtualQueue - Topic name specified");
	}
}
