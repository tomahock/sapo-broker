package pt.com.gcs.messaging;

import java.util.Collection;
import java.util.Date;

import org.caudexorigo.text.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.conf.GcsInfo;

/**
 * QueueLister is responsible for listing all existing queues in an agent.
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

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", DateUtil.formatISODate(new Date()), GcsInfo.getAgentName()));

		for (QueueProcessor qp : QueueProcessorList.values())
		{
			sb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"queue-listing\" value=\"0\" />", qp.getQueueName()));
		}

		sb.append("\n</mqinfo>");

		String result = sb.toString();
		final String sys_topic = String.format("/system/stats/queues/#%s#", GcsInfo.getAgentName());
		InternalPublisher.send(sys_topic, result);
	}
}
