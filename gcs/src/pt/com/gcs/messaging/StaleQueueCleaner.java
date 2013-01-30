package pt.com.gcs.messaging;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaleQueueCleaner implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(StaleQueueCleaner.class);

	@Override
	public void run()
	{
		log.info("Running StaleQueueCleaner");

		long now = System.currentTimeMillis();

		List<String> to_remove = new ArrayList<String>();

		for (final QueueProcessor qp : QueueProcessorList.values())
		{
			long maxStaleAge = qp.getMaxStaleAge();
			long lastDeliveredMsg = qp.lastMessageDelivered();
			long qstaleAge = now - lastDeliveredMsg;

			if (qstaleAge >= maxStaleAge)
			{
				to_remove.add(qp.getQueueName());
			}

			if (log.isDebugEnabled())
			{
				String msg = String.format("Queue '%s' maxStaleAge: %s; lastDeliveredMsg: %s; qstaleAge: %s", qp.getQueueName(), maxStaleAge, lastDeliveredMsg, qstaleAge);
				log.debug(msg);
			}
		}
		int qdelete = to_remove.size();

		for (String queueName : to_remove)
		{
			Gcs.deleteQueue(queueName, false);
			log.debug("Deleted stale queue '{}'", queueName);
		}
		
		log.info("Deleted queues: {}. Number of active queues: {}", qdelete, QueueProcessorList.values().size());
	}
}