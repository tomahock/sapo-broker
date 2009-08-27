package pt.com.broker.performance;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class Consumer implements Callable<Integer>, BrokerListener
{
	private final BrokerClient brokerClient;
	private final DestinationType destinationType;
	private final int numberOfMsgToReceive;

	private AtomicInteger msgReceived;

	private Object obj;

	public Consumer(BrokerClient bkCLient, DestinationType destinationType, int numberOfMsgToReceive)
	{
		this.brokerClient = bkCLient;
		this.destinationType = destinationType;
		this.numberOfMsgToReceive = numberOfMsgToReceive;
	}

	public void init() throws Exception
	{
		msgReceived = new AtomicInteger(0);
		obj = new Object();

		NetSubscribe subscribe = new NetSubscribe("/test/foo", destinationType);
		try
		{
			brokerClient.addAsyncConsumer(subscribe, this);
		}
		catch (Throwable e)
		{
			throw new Exception(e);
		}
	}

	@Override
	public Integer call() throws Exception
	{
		synchronized (obj)
		{
			obj.wait();
		}
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
		int val = msgReceived.incrementAndGet();
		if (val == numberOfMsgToReceive)
		{
			synchronized (obj)
			{
				obj.notify();
			}
		}
	}

}
