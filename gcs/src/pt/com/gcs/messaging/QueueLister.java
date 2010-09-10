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

class QueueLister implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(QueueLister.class);

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
			sb.append(String.format("\n\t<item subject='queue' predicate='listing' value='%s' />", qp.getQueueName()));
		}

		sb.append("\n</mqinfo>");

		String result = sb.toString();

		final String sys_topic = String.format("/system/stats/queues/#%s#", GcsInfo.getAgentName());
		InternalPublisher.send(sys_topic, result);
	}
}
