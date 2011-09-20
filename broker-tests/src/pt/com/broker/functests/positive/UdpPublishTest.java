package pt.com.broker.functests.positive;

import java.util.ArrayList;
import java.util.List;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;

public class UdpPublishTest extends GenericPubSubTest
{
	private BrokerClient client;

	public UdpPublishTest(String testName)
	{
		super(testName);
		HostInfo hostInfo = new HostInfo(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), BrokerTest.getAgent1UdpPort());
		List<HostInfo> hosts = new ArrayList<HostInfo>(1);
		hosts.add(hostInfo);
		try
		{
			client = new BrokerClient(hosts, "tcp://mycompany.com/test", this.getEncodingProtocolType());
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
		return new Action("Publish", "producer")
		{
			public Step run() throws Exception
			{
				try
				{

					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

					NetPublish netPublish = new NetPublish(getDestinationName(), getDestinationType(), brokerMessage);

					((BrokerClient) getInfoProducer()).publishMessageOverUdp(netPublish);

					getInfoProducer().close();

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
		return client;
	}
}
