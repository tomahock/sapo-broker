package pt.com.broker.performance;

import java.util.concurrent.CountDownLatch;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class Consumer extends TestActor
{
	private final DestinationType destinationType;
	private final int numberOfMsgToReceive;

	private CountDownLatch countDown;

	public Consumer(BrokerClient bkCLient, DestinationType destinationType, int numberOfMsgToReceive)
	{
		super(bkCLient);
		this.destinationType = destinationType;
		this.numberOfMsgToReceive = numberOfMsgToReceive;
	}

	public void init() throws Exception
	{
		countDown = new CountDownLatch(numberOfMsgToReceive);

		NetSubscribe subscribe = new NetSubscribe("/test/foo", destinationType);
		try
		{
			getBrokerClient().addAsyncConsumer(subscribe, this);
		}
		catch (Throwable e)
		{
			throw new Exception(e);
		}
	}

	@Override
	public Integer call() throws Exception
	{
		countDown.await();

		return new Integer(0);
	}

	@Override
	public boolean isAutoAck()
	{
		return destinationType != DestinationType.TOPIC;
	}

	@Override
	public void onMessage(NetNotification message)
	{
		// System.out.print(".");
		countDown.countDown();
	}

}
