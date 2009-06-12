package pt.com.broker.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import pt.com.broker.types.NetNotification;

/**
 * SyncConsumer represents a synchronous client.
 *
 */
public class SyncConsumer
{
	private final AtomicInteger count = new AtomicInteger(0);
	private final BlockingQueue<NetNotification> queue = new LinkedBlockingQueue<NetNotification>();

	protected void increment()
	{
		count.incrementAndGet();
	}

	protected void decrement()
	{
		count.decrementAndGet();
	}

	protected int count()
	{
		return count.get();
	}

	protected NetNotification take()
	{
		try
		{
			return queue.take();
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

	protected void offer(NetNotification notification)
	{
		queue.offer(notification);
	}

}
