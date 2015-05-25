package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetProtocolType;

public class TopicNameSpecified extends GenericPubSubTest
{

	public TopicNameSpecified(NetProtocolType protocolType)
	{
		super(protocolType);
		setName("PubSub - Topic name specified");
	}

}
