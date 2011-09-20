package pt.com.broker.performance;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;

public class Producer extends TestActor
{
	private final DestinationType destinationType;
	private final int numberOfMsgToSend;
	private final String message;

	public Producer(BrokerClient bkCLient, DestinationType destinationType, int numberOfMsgToSend, String message)
	{
		super(bkCLient);
		this.destinationType = destinationType;
		this.numberOfMsgToSend = numberOfMsgToSend;
		this.message = message;
	}

	@Override
	public Integer call() throws Exception
	{
		NetBrokerMessage message = new NetBrokerMessage(this.message.getBytes());
		String destination = "/test/foo";
		if (destinationType == DestinationType.QUEUE)
		{
			for (int i = numberOfMsgToSend; i != 0; --i)
			{
				getBrokerClient().enqueueMessage(message, destination);
			}
		}
		else
		{
			for (int i = numberOfMsgToSend; i != 0; --i)
			{
				getBrokerClient().publishMessage(message, destination);
			}
		}

		return new Integer(0);
	}

	@Override
	public boolean isAutoAck()
	{
		return false;
	}

	@Override
	public void onMessage(NetNotification message)
	{
	}

}
