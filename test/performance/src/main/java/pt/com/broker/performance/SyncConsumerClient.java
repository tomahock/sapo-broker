package pt.com.broker.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetNotification;

public class SyncConsumerClient implements Runnable
{

	private AtomicInteger counter;
	private String clientId;
	private String host;
	private int port;
	private final String queueName;
	private final CountDownLatch countDown;

	private static AtomicInteger clientsEnded = new AtomicInteger(0);

	public SyncConsumerClient(AtomicInteger counter, String clientId, String host, int port, String queueName, CountDownLatch countDown)
	{
		super();
		this.counter = counter;
		this.clientId = clientId;
		this.host = host;
		this.port = port;
		this.queueName = queueName;
		this.countDown = countDown;
	}

	@Override
	public void run()
	{
		System.out.printf("SyncConsumerClient '%s' started%n", clientId);

		long pollTimeAcc = 0;
		int timeAccCount = 0;

		BrokerClient bk = null;
		try
		{
			bk = new BrokerClient(host, port);

			while (counter.get() > 0)
			{

				long initNanoTime = System.nanoTime();
				NetNotification poll = bk.poll(queueName, -1, null);
				long endNanoTime = System.nanoTime();

				pollTimeAcc += (endNanoTime - initNanoTime);
				++timeAccCount;

				if (poll != null)
				{
					bk.acknowledge(poll);
					long currentCount = counter.decrementAndGet();

					if ((currentCount % 50) == 0)
						System.out.println(currentCount);
				}

			}

			double v = (double) ((pollTimeAcc / timeAccCount) / (1000 * 1000));

			if (timeAccCount != 0)
				System.out.printf("SyncConsumerClient '%s' ended. Average latency:%4.2fms %n", clientId, v);
			else
				System.out.printf("SyncConsumerClient '%s' ended. No messages received");

		}
		catch (TimeoutException ex)
		{
			ex.printStackTrace();
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();

		}
		finally
		{
			if (bk != null)
			{
				bk.close();

			}
			// System.out.println("Clients that ended: " +clientsEnded.incrementAndGet());
			countDown.countDown();
		}
	}

}
