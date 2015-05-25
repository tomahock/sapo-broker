package pt.com.broker.performance;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class PollPerformanceTestV1
{
	private static String hostname = "localhost";
	private static int port = 3323;

	private static final int NUMBER_OF_CONSUMERS = 128;

	private static final int MESSAGES_PRODUCED = 1500;

	private static final String QUEUE_NAME = "/queue/performance";

	private static boolean produceBefore = true; // true if messages should be produced before consumers
	private static Object producingSynObj = new Object();

	private static volatile long initTime = 0; // possible check-and-act. Not critical
	private static AtomicInteger messagesReceived = new AtomicInteger(0);
	private static volatile long endTime = 0;

	private static Object endSyncObj = new Object();

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
					bk.enqueueMessage(brokerMessage, QUEUE_NAME);
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

	public static class Consumer extends Thread
	{
		@Override
		public void run()
		{
			System.out.println("Consumer Thread started");
			int messagesReceivedByThread = 0;

			long pollTimeAcc = 0;
			int timeAccCount = 0;
			BrokerClient bk = null;
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

				while (messagesReceived.get() != MESSAGES_PRODUCED)
				{
					try
					{
						long initNanoTime = System.nanoTime();
						NetNotification poll = bk.poll(QUEUE_NAME, -1, null);
						long endNanoTime = System.nanoTime();

						pollTimeAcc += (endNanoTime - initNanoTime);
						++timeAccCount;

						// System.out.print(".");
						if (poll != null)
						{
							bk.acknowledge(poll);
							long currentCount = messagesReceived.addAndGet(1);
							++messagesReceivedByThread;

							if ((currentCount % 50) == 0)
								System.out.println(currentCount);
							if (currentCount == MESSAGES_PRODUCED)
							{
								endTime = System.nanoTime();
								if (timeAccCount != 0)
									System.out.println("Consumer Thread ended (limit reached). Average:" + (pollTimeAcc / timeAccCount));
								else
									System.out.println("Consumer Thread ended (limit reached). No messages received");
								synchronized (endSyncObj)
								{
									endSyncObj.notifyAll();
								}
								bk.close();
								return;
							}
						}
						else
						{
							if (timeAccCount != 0)
								System.out.println("Consumer Thread ended (null message). Average:" + (pollTimeAcc / timeAccCount));
							else
								System.out.println("Consumer Thread ended (null message).  No messages received.");
							bk.close();
							break;
						}
					}
					catch (TimeoutException te)
					{
						if (messagesReceived.addAndGet(0) >= MESSAGES_PRODUCED)
						{
							if (timeAccCount != 0)
								System.out.println("Consumer Thread ended (timeout). Average:" + (pollTimeAcc / timeAccCount));
							else
								System.out.println("Consumer Thread ended (timeout). No messages received");
							bk.close();
							return;
						}
					}
				}

			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (timeAccCount != 0)
					System.out.println("Consumer Thread ended (finally). Average:" + (pollTimeAcc / timeAccCount));
				else
					System.out.println("Consumer Thread ended (finally).  No messages received.");
				bk.close();
			}
		}
	};

	public static class AsynConsumer extends Thread
	{
		@Override
		public void run()
		{
			System.out.println("AsynConsumer Thread started");
			Object endObj = new Object();
			BrokerClient bk = null;
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
					NetSubscribe subscribe = new NetSubscribe(QUEUE_NAME, DestinationType.QUEUE);

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
							long currentCount = messagesReceived.addAndGet(1);

							if ((currentCount % 50) == 0)
								System.out.println(currentCount);
							if (currentCount == MESSAGES_PRODUCED)
							{
								endTime = System.nanoTime();
								System.out.println("Consumer Thread ended (limit reached). ");

								synchronized (endSyncObj)
								{
									endSyncObj.notifyAll();
								}

								return;
							}

						}

					};

					bk.addAsyncConsumer(subscribe, listener);

					synchronized (endObj)
					{
						endObj.wait();
					}
					bk.close();
				}
				catch (TimeoutException te)
				{
					if (messagesReceived.addAndGet(0) >= MESSAGES_PRODUCED)
					{
						System.out.println("Consumer Thread ended (timeout).");
						bk.close();
						return;
					}
				}

			}
			catch (Throwable e)
			{
				e.printStackTrace();
				bk.close();
				System.out.println("Consumer Thread ended (exception):");
				return;
			}
		}
	};

	public static void main(String[] args)
	{
		System.out.println("Test starting!");

		new Producer().start();

		int consumerCount = NUMBER_OF_CONSUMERS;

		while ((consumerCount--) != 0)
		{
			new Consumer().start();
			// new AsynConsumer().start();
		}

		synchronized (endSyncObj)
		{
			try
			{
				endSyncObj.wait();
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
