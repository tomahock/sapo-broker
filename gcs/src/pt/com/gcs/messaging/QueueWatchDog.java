package pt.com.gcs.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueWatchDog implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(QueueWatchDog.class);

	private static final long MAX_TIME = 2 * 60 * 1000; // Max time that a queue can be without delivering messages having ready consumers;

	@Override
	public void run()
	{
		long x = System.currentTimeMillis() - MAX_TIME;

		for (final QueueProcessor qp : QueueProcessorList.values())
		{
			if ((qp.getQueuedMessagesCount() > 0) && (qp.getLastCycle() < x) && (qp.hasRecipient()))
			{
				log.info(String.format("Watchdog started message deliver to queue '%s' ", qp.getQueueName()));
				qp.deliverMessages();
			}
		}
	}
}
