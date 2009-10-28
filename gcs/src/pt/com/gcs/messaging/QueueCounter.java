package pt.com.gcs.messaging;

import java.util.Collection;

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

		for (QueueProcessor qp : qpl)
		{
			long cnt = qp.getQueuedMessagesCount();
			if (cnt > 0)
			{
				log.info("Queue '{}' has {} message(s).", qp.getDestinationName(), cnt);
				publishCount(qp, cnt);
			}
			else if ((cnt == 0) && !qp.emptyQueueInfoDisplay.getAndSet(true))
			{
				log.info("Queue '{}' is empty.", qp.getDestinationName());
				publishCount(qp, cnt);
			}

		}
	}

	private void publishCount(QueueProcessor qp, long cnt)
	{
		try
		{
			String dName = String.format("/system/stats/queue-size/#%s#", qp.getDestinationName());
			String content = GcsInfo.getAgentName() + "#" + qp.getDestinationName() + "#" + cnt;

			InternalPublisher.send(dName, content);
		}
		catch (Throwable error)
		{
			String emsg = String.format("Could not publish queue counter for '{}'", qp.getDestinationName());
			log.error(emsg, error);
		}
	}
}
