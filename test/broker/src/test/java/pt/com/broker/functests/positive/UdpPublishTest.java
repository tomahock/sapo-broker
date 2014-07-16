package pt.com.broker.functests.positive;


import pt.com.broker.functests.Action;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.UdpBrokerClient;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetProtocolType;

import java.util.concurrent.Future;

public class UdpPublishTest extends GenericPubSubTest
{
	private UdpBrokerClient client;

    public UdpPublishTest(NetProtocolType protocolType) {
        super(protocolType);


		try
		{

			client = new UdpBrokerClient(getAgent1Hostname(), BrokerTest.getAgent1UdpPort(),getEncodingProtocolType());


		}
		catch (Throwable e)
		{
			setFailure(e);
		}

		super.dataLenght = 250;

	}

	@Override
	public Action getAction()
	{
		final UdpBrokerClient uclient = client;
		
		return new Action("Publish", "producer")
		{
			public Step run() throws Exception
			{
				try
				{

					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

					Future f = uclient.publish(brokerMessage, getDestinationName(), getDestinationType());


                    f.get();

                    Thread.sleep(4000);


					setDone(true);
					setSucess(true);
				}
				catch (Throwable t)
				{
					throw new Exception(t);
				}
				return this;
			}
		};
	}

	@Override
	public BrokerClient getInfoProducer()
	{
		// return client;
		return null;
	}
}
