package pt.com.gcs.messaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class StaleQueueCleaner implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(StaleQueueCleaner.class);

	private Optional<String> queuePrefix;
	private Long queueMaxStaleAge;

	public StaleQueueCleaner(Optional<String> queuePrefix, Long queueMaxStaleAge)
	{
		Preconditions.checkNotNull(queuePrefix, "The queuePrefix cannot be null.");
		Preconditions.checkNotNull(queueMaxStaleAge, "The queueMaxStaleAge cannot be null.");
		this.queuePrefix = queuePrefix;
		this.queueMaxStaleAge = queueMaxStaleAge;
	}

	private Collection<QueueProcessor> getProcessors()
	{
		if (queuePrefix.isPresent())
		{
			return QueueProcessorList.findByPattern(queuePrefix.get());
		}
		else
		{
			return QueueProcessorList.values();
		}
	}

	@Override
	public void run()
	{
		if (log.isDebugEnabled())
		{
			log.debug("Running Stale Queue Cleaner: {}", queuePrefix.isPresent() ? queuePrefix.get() : "global");
		}
		long now = System.currentTimeMillis();
		List<String> to_remove = new ArrayList<String>();
		Collection<QueueProcessor> processors = getProcessors();

		for (final QueueProcessor qp : processors)
		{
			long lastDeliveredMsg = qp.lastMessageDelivered();
			long qstaleAge = now - lastDeliveredMsg;

			// If the stale age have expired and we have a specific setting for the queue prefix or the
			// queue does not have any consumers, we delete the queue.
			if (qstaleAge >= queueMaxStaleAge && (queuePrefix.isPresent() || !QueueProcessorList.get(qp.getQueueName()).hasRecipient()))
			{
				to_remove.add(qp.getQueueName());
			}

			if (log.isDebugEnabled())
			{
				String msg = String.format("Queue '%s' maxStaleAge: %s; lastDeliveredMsg: %s; qstaleAge: %s", qp.getQueueName(), queueMaxStaleAge, lastDeliveredMsg, qstaleAge);
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