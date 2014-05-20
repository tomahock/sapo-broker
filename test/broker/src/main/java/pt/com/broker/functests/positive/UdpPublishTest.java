package pt.com.broker.functests.positive;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.UdpClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;

public class UdpPublishTest extends GenericPubSubTest
{
	private  UdpClient client;

	public UdpPublishTest(String testName)
	{
		super(testName);

		try
		{
            boolean isOldFramming =  getEncodingProtocolType().equals(NetProtocolType.SOAP_v0);

			client = new UdpClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1UdpPort(),isOldFramming);
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
		final UdpClient uclient = client;
		
		return new Action("Publish", "producer")
		{
			public Step run() throws Exception
			{
				try
				{

					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

					NetPublish netPublish = new NetPublish(getDestinationName(), getDestinationType(), brokerMessage);

					uclient.publish(netPublish);

                    System.out.println("cenas");

					//getInfoProducer().close();

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
	public BaseBrokerClient getInfoProducer()
	{
		// return client;
		return null;
	}
}
