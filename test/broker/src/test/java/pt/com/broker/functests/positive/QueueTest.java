package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class QueueTest extends GenericPubSubTest
{

    public QueueTest(NetProtocolType protocolType) {
        super(protocolType);

        setName("Queue with single recipient");
        setDestinationType(DestinationType.QUEUE);
    }

}
