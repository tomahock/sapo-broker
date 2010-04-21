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
		
		final String queueSubscriptionTopic = "/system/stats/queues/";
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<qinfo date='%s' agent-name='%s'>", DateUtil.formatISODate(new Date()), GcsInfo.getAgentName()));
		
		for (QueueProcessor qp : QueueProcessorList.values())
		{
			long cnt = qp.getQueuedMessagesCount();
			
			if (cnt > 0)
			{
				log.info("Queue '{}' has {} message(s).", qp.getQueueName(), cnt);
				sb.append(String.format("\n	<item subject='queue://%s' predicate='queue-size' value='%s' />", qp.getQueueName(), cnt));
			}
			else if ((cnt == 0)/* && !qp.emptyQueueInfoDisplay.getAndSet(true)*/)
			{
				log.info("Queue '{}' is empty.", qp.getQueueName());
				sb.append(String.format("\n	<item subject='queue://%s' predicate='queue-size' value='%s' />", qp.getQueueName(), cnt));			
			}
		}
		
		sb.append("\n</qinfo>");
		
		String result = sb.toString();
		InternalPublisher.send(String.format("%s#%s#", queueSubscriptionTopic, GcsInfo.getAgentName()), result);		
	}
}
