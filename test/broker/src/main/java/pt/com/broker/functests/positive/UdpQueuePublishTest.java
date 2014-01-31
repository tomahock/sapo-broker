package pt.com.broker.functests.positive;

import pt.com.broker.types.NetAction.DestinationType;

public class UdpQueuePublishTest extends UdpPublishTest
{

	public UdpQueuePublishTest()
	{
		super("UDP queue publication");
		setDestinationType(DestinationType.QUEUE);
	}
}