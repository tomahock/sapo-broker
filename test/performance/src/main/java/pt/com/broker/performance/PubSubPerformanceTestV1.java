package pt.com.broker.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class PubSubPerformanceTestV1
{
	private static String hostname = "localhost";
	private static int port = 3323;

	private static final int NUMBER_OF_CONSUMERS = 4;

	private static final int MESSAGES_PRODUCED = 50;

	private static final String DESTINATION_NAME = "/topic/performance";

	private static boolean produceBefore = true; // true if messages should be produced before consumers
	private static Object producingSynObj = new Object();

	private static volatile long initTime = 0; // possible check-and-act. Not critical
	private static volatile long endTime = 0;

	private static Object endSyncObj = new Object();

	private static CountDownLatch consumerThreadRegistered = new CountDownLatch(0);

	public static class Producer extends Thread
	{

		@Override
		public void run()
		{
			System.out.println("Producer Thread started");
			try
			{
				BrokerClient bk = new BrokerClient(hostname, port);

				int count = MESSAGES_PRODUCED;

				NetBrokerMessage brokerMessage = new NetBrokerMessage("This is a test!");

				while ((count--) != 0)
				{
					System.out.print("P");
					bk.publishMessage(brokerMessage, DESTINATION_NAME);
					Sleep.time(10);
				}
				if (produceBefore)
				{
					synchronized (producingSynObj)
					{
						producingSynObj.notifyAll();
					}
				}
				bk.close();

			}
			catch (Throwable e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Producer Thread ended");
		}
	};

	public static class AsynConsumer extends Thread
	{
		private volatile int messagesReceived = 0;
		private BrokerClient bk = null;

		@Override
		public void run()
		{
			System.out.println("AsynConsumer Thread started");
			try
			{
				bk = new BrokerClient(hostname, port);
				if (produceBefore)
				{
					synchronized (producingSynObj)
					{
						producingSynObj.wait();
					}
				}

				if (initTime == 0)
				{
					initTime = System.nanoTime();
				}

				try
				{
					NetSubscribe subscribe = new NetSubscribe(DESTINATION_NAME, DestinationType.TOPIC);

					BrokerListener listener = new BrokerListener()
					{

						@Override
						public boolean isAutoAck()
						{
							return true;
						}

						@Override
						public void onMessage(NetNotification message)
						{
							System.out.print("C");
							if (++messagesReceived == MESSAGES_PRODUCED)
							{
								synchronized (endSyncObj)
								{
									endSyncObj.notifyAll();
									bk.close();
								}
							}
						}

					};

					bk.addAsyncConsumer(subscribe, listener);
					consumerThreadRegistered.countDown();
				}
				catch (TimeoutException te)
				{
					te.printStackTrace();
				}

			}
			catch (Throwable e)
			{
				e.printStackTrace();
				bk.close();
				System.out.println("Consumer Thread ended (exception):");
				return;
			}
			System.out.println("AsynConsumer.run() END");
		}
	};

	public static void main(String[] args)
	{
		System.out.println("Test starting!");

		int consumerCount = NUMBER_OF_CONSUMERS;

		while ((consumerCount--) != 0)
		{
			new AsynConsumer().start();
		}

		try
		{
			consumerThreadRegistered.await();
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
			Shutdown.now();
		}

		new Producer().start();

		synchronized (endSyncObj)
		{
			try
			{
				endSyncObj.wait();
				endTime = System.nanoTime();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

		}

		System.out.println("  Test Ended!");
		System.out.println("     Messages produced: " + MESSAGES_PRODUCED);
		System.out.println("     Number of consumer: " + NUMBER_OF_CONSUMERS);
		System.out.println("     Time (in milliseconds): " + (endTime - initTime) / (1000 * 1000));
	}
}
