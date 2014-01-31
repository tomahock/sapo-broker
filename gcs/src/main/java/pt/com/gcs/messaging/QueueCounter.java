package pt.com.gcs.messaging;

import java.util.Collection;
import java.util.Date;

import org.caudexorigo.text.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GcsInfo;

/**
 * QueueCounter is responsible for counting and publishing the total number number of messages per queue.
 * 
 */

class QueueCounter implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(QueueCounter.class);

	@Override
	public void run()
	{
		Collection<QueueProcessor> qpl = QueueProcessorList.values();

		log.debug("Number of registered Queues: {}", qpl.size());

		// New format

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", DateUtil.formatISODate(new Date()), GcsInfo.getAgentName()));

		for (QueueProcessor qp : QueueProcessorList.values())
		{
			long cnt = qp.getQueuedMessagesCount();
			boolean sendstats = true;

			if (cnt == 0)
			{
				if (!qp.emptyQueueInfoDisplay.getAndSet(true))
				{
					log.info("Queue '{}' is empty.", qp.getQueueName());
				}
				else
				{
					sendstats = false;
				}
			}
			else if (cnt == 1)
			{
				log.info("Queue '{}' has 1 message.", qp.getQueueName());
			}
			else if (cnt > 1)
			{
				log.info("Queue '{}' has {} messages.", qp.getQueueName(), cnt);
			}

			if (sendstats)
			{
				sb.append(String.format("\n\t<item subject='queue://%s' predicate='queue-size' value='%s' />", qp.getQueueName(), cnt));
			}
		}

		sb.append("\n</mqinfo>");

		String result = sb.toString();

		final String sys_topic = String.format("/system/stats/queues/#%s#", GcsInfo.getAgentName());
		InternalPublisher.send(sys_topic, result);
	}
}
