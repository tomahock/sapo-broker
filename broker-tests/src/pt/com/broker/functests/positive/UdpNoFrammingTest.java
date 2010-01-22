package pt.com.broker.functests.positive;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import pt.com.broker.functests.Action;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetAction.ActionType;

public class UdpNoFrammingTest extends GenericPubSubTest
{

	private BindingSerializer serializer = null;
	
	
	public UdpNoFrammingTest()
	{
		this("UDP legacy framing test");
	}
	
	public UdpNoFrammingTest(String testName)
	{
		super(testName);
		try
		{
			serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.xml.SoapBindingSerializer").newInstance();
		}
		catch (Throwable t)
		{
			
		}
		
	}
	
	private void publishMessage(NetPublish message)
	{
		System.out.println("UdpNoFrammingTest.publishMessage()");
		if(message == null)
		{ 
			setFailure(new Exception("Failed to load BindingSerializer"));
		}
		
		NetAction action = new NetAction(ActionType.PUBLISH);
		action.setPublishMessage(message);

		NetMessage netMessage = new NetMessage(action);

		try
		{
			byte[] serializedData = serializer.marshal(netMessage);

			InetAddress inet = InetAddress.getByName(  getInfoConsumer().getNetHandler().getHostInfo().getHostname());
			DatagramSocket socket = new DatagramSocket();
			socket.setSoTimeout(5000);
			DatagramPacket packet = new DatagramPacket(serializedData, serializedData.length, inet, Integer.parseInt(ConfigurationInfo.getParameter("agent1-legacy-udp-port")));
			socket.send(packet);
			socket.close();
		}
		catch (Throwable t)
		{
			setFailure(new Exception("Error processing UDP message", t));
		}
	}
	
	@Override
	protected void addAction()
	{
		this.setAction(new Action("Publish", "producer")
		{
			public Step run() throws Exception
			{
				try
				{
					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());
					
					NetPublish publishMessage = new NetPublish(getDestinationName(), getDestinationType(), brokerMessage);

					
					publishMessage(publishMessage);
					
					setDone(true);
					setSucess(true);
				}
				catch (Throwable t)
				{
					throw new Exception(t);
				}
				return this;

			}
		});
	}
	
	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() != NetProtocolType.SOAP) && (getEncodingProtocolType() != NetProtocolType.SOAP_v0);
	}

}
