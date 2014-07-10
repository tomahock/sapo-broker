package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;
import pt.com.broker.types.NetProtocolType;

public class VirtualQueueNameSpecified extends MultipleGenericVirtualQueuePubSubTest
{

    public VirtualQueueNameSpecified(NetProtocolType protocolType) {
        super(protocolType);

        setName("VirtualQueue - Topic name specified");
    }


}
