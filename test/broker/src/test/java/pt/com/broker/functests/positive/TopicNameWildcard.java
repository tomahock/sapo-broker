package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

public class TopicNameWildcard extends GenericPubSubTest
{

	public TopicNameWildcard(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("PubSub - Topic name is a wildcard");
		setSubscriptionName("/topic/.*");
	}
}
