package pt.com.broker.performance;

import java.util.concurrent.CountDownLatch;

import org.caudexorigo.Shutdown;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetBrokerMessage;

public class QueueProducer implements Runnable
{

	private int limit;
	private final String host;
	private final int port;
	private final String queueName;
	private final CountDownLatch countDown;

	public QueueProducer(int limit, String host, int port, String queueName, CountDownLatch countDown)
	{
		super();
		this.limit = limit;
		this.host = host;
		this.port = port;
		this.queueName = queueName;
		this.countDown = countDown;

	}

	@Override
	public void run()
	{
		System.out.printf("QueueProducer started%n");
		BrokerClient bk = null;
		int ix = 0;
		try
		{
			bk = new BrokerClient(host, port);
			NetBrokerMessage brokerMessage = new NetBrokerMessage("This is a test!");

			while (limit-- > 0)
			{
				bk.enqueueMessage(brokerMessage, queueName);
				ix++;
			}

			countDown.countDown();

		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
			Shutdown.now();

		}
		finally
		{
			if (bk != null)
			{
				bk.close();

			}
		}
		System.out.printf("QueueProducer ended. %s messages were sent.%n", ix);
	}
}