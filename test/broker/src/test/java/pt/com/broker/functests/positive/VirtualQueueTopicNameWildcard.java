package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;
import pt.com.broker.types.NetProtocolType;

public class VirtualQueueTopicNameWildcard extends MultipleGenericVirtualQueuePubSubTest
{

	public VirtualQueueTopicNameWildcard(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("VirtualQueue - Topic name is a wildcard");

		setSubscriptionName(String.format("xpto@/%s/.*", getBaseName()));
	}
}
