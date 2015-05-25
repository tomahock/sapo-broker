package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.MultipleGenericQueuePubSubTest;
import pt.com.broker.types.NetProtocolType;

public class RemoteQueueTest extends MultipleGenericQueuePubSubTest
{
	public RemoteQueueTest(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("Remote queue consumer");
	}

}
