package pt.com.broker.functests.positive;

import java.util.ArrayList;
import java.util.List;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.HostInfo;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetAction.DestinationType;

public class UdpPublishTest extends GenericPubSubTest
{
	private BrokerClient client;
	
	public UdpPublishTest(String testName)
	{
		super(testName);
		
		HostInfo hostInfo = new HostInfo("localhost", 3323, 3366);
		List<HostInfo> hosts = new ArrayList<HostInfo>(1);
		hosts.add(hostInfo);
		try
		{
			client =  new BrokerClient(hosts, "tcp://mycompany.com/mypublisher", this.getEncodingProtocolType());
		}
		catch (Throwable e)
		{
			setFailure(e);
		}
		
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

					NetPublish netPublish = new NetPublish(getDestinationName(),getDestinationType(), brokerMessage);
					
					((BrokerClient)getInfoProducer()).publishMessageOverUdp(netPublish);
					
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
