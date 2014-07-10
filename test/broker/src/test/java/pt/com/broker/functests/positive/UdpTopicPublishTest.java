package pt.com.broker.functests.positive;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class UdpTopicPublishTest extends UdpPublishTest
{

    public UdpTopicPublishTest(NetProtocolType protocolType) {
        super(protocolType);

        setName("UDP topic publication");

		setDestinationType(DestinationType.TOPIC);
	}
}
