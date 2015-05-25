package pt.com.broker.functests.positive;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetProtocolType;

public class TopicNameSpecifiedDist extends TopicNameSpecified
{

	public TopicNameSpecifiedDist(NetProtocolType protocolType)
	{
		super(protocolType);

		setName("PubSub - Topic name specified with distant consumer");
		try
		{
			BrokerClient client = new BrokerClient(getAgent1Hostname(), getAgent1Port(), getEncodingProtocolType());

			client.connect();

			setInfoConsumer(client);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}
}
