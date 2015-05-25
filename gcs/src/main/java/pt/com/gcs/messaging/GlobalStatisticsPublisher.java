package pt.com.gcs.messaging;

import java.util.Date;

import org.caudexorigo.time.ISO8601;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.stats.ChannelStatistics;
import pt.com.gcs.stats.QueueStatistics;
import pt.com.gcs.stats.Statistics;
import pt.com.gcs.stats.TopicStatistics;

public class GlobalStatisticsPublisher
{
	private static Logger log = LoggerFactory.getLogger(GlobalStatisticsPublisher.class);

	private Date currentDate;
	private Date oldDate;
	private Statistics statistics;

	public GlobalStatisticsPublisher(Date oldDate, Date currentDate, Statistics statistics)
	{
		this.oldDate = oldDate;
		this.currentDate = currentDate;
		this.statistics = statistics;
	}

	public void publishStatistics()
	{

		long difSeconds = (currentDate.getTime() - oldDate.getTime()) / 1000;

		String currentDateStr = ISO8601.format(currentDate);

		publishQueueInfo(currentDateStr, difSeconds);

		publishTopicInfo(currentDateStr, difSeconds);

		publishChannelInfo(currentDateStr, difSeconds);

		// publishEncodingInfo(currentDateStr, difSeconds);

		publishMiscInformation(currentDateStr, difSeconds);
	}

	private void publishQueueInfo(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();

		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));

		double rate;
		long value;
		for (QueueStatistics queueStats : statistics.getQueueStatistics())
		{
			StringBuilder qSb = new StringBuilder();
			int infoCount = 0;

			value = queueStats.getReceivedMessages();
			if (value != -1)
			{
				rate = ((double) value / dSeconds);
				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"input-rate\" value=\"%s\" />", queueStats.getQueueName(), rate));
				++infoCount;
			}

			value = queueStats.getDeliveredMessages();
			if (value != -1)
			{
				rate = ((double) value / dSeconds);
				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"output-rate\" value=\"%s\" />", queueStats.getQueueName(), rate));
				++infoCount;
			}

			value = queueStats.getExpiredMessages();
			if (value != -1)
			{
				rate = ((double) value / dSeconds);
				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"expired-rate\" value=\"%s\" />", queueStats.getQueueName(), rate));
				++infoCount;
			}

			value = queueStats.getRedeliveredMessages();
			if (value != -1)
			{
				rate = ((double) value / dSeconds);
				qSb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"redelivered-rate\" value=\"%s\" />", queueStats.getQueueName(), rate));
				++infoCount;
			}

			if (infoCount != 0)
			{
				sb.append(qSb.toString()); // Add queue information only when something is different from zero
			}
		}

		// queue count (number of queues in this agent)
		sb.append(String.format("\n	<item subject=\"queue\" predicate=\"count\" value=\"%s\" />", statistics.getQueueStatistics().size()));

		sb.append("\n</mqinfo>");

		String result = sb.toString();

		final String sys_topic = String.format("/system/stats/queues/#%s#", GcsInfo.getAgentName());

		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));

		Gcs.publish(np);
	}

	private void publishTopicInfo(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();

		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));

		double rate;
		long value;

		rate = ((double) statistics.getSystemStatistics().getReceivedTopicMessages() / dSeconds);

		sb.append(String.format("\n\t<item subject='topic://.*' predicate='input-rate' value='%s' />", rate));
		for (TopicStatistics topicStats : statistics.getTopicStatistics())
		{
			StringBuilder tSb = new StringBuilder();
			int infoCount = 0;

			value = topicStats.getDeliveredMessages();
			if (value != -1)
			{
				rate = ((double) value / dSeconds);
				tSb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"output-rate\" value=\"%s\" />", topicStats.getTopicName(), rate));
				++infoCount;
			}

			value = topicStats.getDiscardedMessages();
			if (value != -1)
			{
				rate = ((double) value / dSeconds);
				tSb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"discarded-rate\" value=\"%s\" />", topicStats.getTopicName(), rate));
				++infoCount;
			}

			value = topicStats.getDispatchedToQueueMessages();
			if (value != -1)
			{
				rate = ((double) value / dSeconds);
				tSb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"dispatched-to-queue-rate\" value=\"%s\" />", topicStats.getTopicName(), rate));
				++infoCount;
			}
			if (infoCount != 0)
			{
				sb.append(tSb.toString()); // Add queue information only when something is different from zero
			}
		}

		sb.append("\n</mqinfo>");

		String result = sb.toString();

		final String sys_topic = String.format("/system/stats/topics/#%s#", GcsInfo.getAgentName());

		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));

		Gcs.publish(np);
	}

	private void publishChannelInfo(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();

		for (ChannelStatistics channelStats : statistics.getChannelStatistics())
		{
			sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));

			double rate;

			rate = ((double) channelStats.getReceivedMessages() / dSeconds);
			sb.append(String.format("\n\t<item subject='%s' predicate='input-rate' value='%s' />", channelStats.getChannelName(), rate));
			// rate = ((double) ChannelStats.getHttpReceivedMessagesAndReset() / dSeconds);
			// sb.append(String.format("\n\t<item subject='http' predicate='input-rate' value='%s' />", rate));

			sb.append("\n</mqinfo>");
		}

		String result = sb.toString();

		final String sys_topic = String.format("/system/stats/channels/#%s#", GcsInfo.getAgentName());
		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));

		Gcs.publish(np);
	}

	private void publishMiscInformation(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("<mqinfo date='%s' agent-name='%s'>", date, GcsInfo.getAgentName()));

		double rate;
		// invalid messages
		rate = ((double) statistics.getSystemStatistics().getInvalidMessages() / dSeconds);
		sb.append(String.format("\n\t<item subject='invalid-messages' predicate='input-rate' value='%s' />", rate));

		// access denied
		rate = ((double) statistics.getSystemStatistics().getAccessDeniedMessages());
		if (rate > 0)
		{
			sb.append(String.format("\n\t<item subject='access' predicate='denied' value='%s' />", rate));
		}

		// tcp, tcp-legacy, ssl
		sb.append(String.format("\n\t<item subject='tcp' predicate='connections' value='%s' />", statistics.getSystemStatistics().getTcpConnections()));
		sb.append(String.format("\n\t<item subject='tcp-legacy' predicate='connections' value='%s' />", statistics.getSystemStatistics().getTcpLegacyConnections()));
		sb.append(String.format("\n\t<item subject='ssl' predicate='connections' value='%s' />", statistics.getSystemStatistics().getSslConnections()));

		long f_sys_msgs = statistics.getSystemStatistics().getFailedMessages();

		// System messages - failed delivery (count)
		sb.append(String.format("\n\t<item subject='system-message' predicate='failed-delivery' value='%s' />", f_sys_msgs));

		// faults (rate)
		rate = ((double) statistics.getSystemStatistics().getSystemFaults() / dSeconds);
		sb.append(String.format("\n\t<item subject='faults' predicate='rate' value='%s' />", rate));

		sb.append("\n</mqinfo>");

		String result = sb.toString();

		final String sys_topic = String.format("/system/stats/misc/#%s#", GcsInfo.getAgentName());
		NetPublish np = new NetPublish(sys_topic, DestinationType.TOPIC, new NetBrokerMessage(result));

		Gcs.publish(np);

		log.info("Failed system messages: '{}'.", f_sys_msgs);
	}
}