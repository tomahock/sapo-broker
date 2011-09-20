package pt.com.broker.functests.positive;

import pt.com.broker.types.NetAction.DestinationType;

public class UdpTopicPublishTest extends UdpPublishTest
{

	public UdpTopicPublishTest()
	{
		super("UDP topic publication");
		setDestinationType(DestinationType.TOPIC);
	}
}
