package pt.com.broker.functests.positive;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class UdpQueuePublishTest extends UdpPublishTest
{


    public UdpQueuePublishTest(NetProtocolType protocolType) {
        super(protocolType);

        setName("UDP queue publication");
		setDestinationType(DestinationType.QUEUE);
	}
}