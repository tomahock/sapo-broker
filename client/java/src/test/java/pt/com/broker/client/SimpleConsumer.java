package pt.com.broker.client;

import java.io.UnsupportedEncodingException;

import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;

public class SimpleConsumer
{

	public static void main(String[] args) throws Throwable
	{
		BrokerClient bk = new BrokerClient("127.0.0.1", 3323, "Test", NetProtocolType.PROTOCOL_BUFFER);
		bk.addAsyncConsumer(new NetSubscribe("/sapo/broker/dev/test_node", DestinationType.TOPIC), new BrokerListener()
		{

			@Override
			public void onMessage(NetNotification message)
			{
				try
				{
					String msg = new String(message.getMessage().getPayload(), "US-ASCII");
					System.out.println("Message: " + msg);
				}
				catch (UnsupportedEncodingException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public boolean isAutoAck()
			{
				// TODO Auto-generated method stub
				return true;
			}
		});
	}

}
