package pt.com.broker.functests.positive;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetProtocolType;

public class TopicNameWildcardDist extends TopicNameWildcard
{


    public TopicNameWildcardDist(NetProtocolType protocolType) {
        super(protocolType);

        setName("PubSub - Topic name is a wildcard with remote consumer");
		try
		{
            BrokerClient bk = new BrokerClient(getAgent2Hostname(), getAgent2Port(), getEncodingProtocolType());

            bk.connect();

			setInfoConsumer(bk);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
