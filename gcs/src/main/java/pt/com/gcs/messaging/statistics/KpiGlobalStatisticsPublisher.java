package pt.com.gcs.messaging.statistics;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.stats.ChannelStatistics;
import pt.com.gcs.stats.QueueStatistics;
import pt.com.gcs.stats.Statistics;
import pt.com.gcs.stats.SystemStatistics;
import pt.com.gcs.stats.TopicStatistics;
import pt.sapo.socialbus.common.kpi.EventBuilder;
import pt.sapo.socialbus.common.kpi.data.Event;
import pt.sapo.socialbus.common.kpi.data.MetricItem;
import pt.sapo.socialbus.common.kpi.data.MetricType;

import com.google.common.collect.Lists;

/**
 * Collects global agent statistics and publishes them to the KPI central.
 * */
public class KpiGlobalStatisticsPublisher
{

	private static final Logger log = LoggerFactory.getLogger(KpiGlobalStatisticsPublisher.class);

	private final Date date;
	private final Statistics statistics;

	public KpiGlobalStatisticsPublisher(Date date, Statistics statistics)
	{
		this.date = date;
		this.statistics = statistics;
	}

	public void publishStatistics()
	{
		List<Event> kpiEvents = Lists.newArrayList();
		kpiEvents.addAll(getQeueInfoKpis());
		kpiEvents.addAll(getTopicInfoKpis());
		kpiEvents.addAll(getChannelInfoKpis());
		kpiEvents.addAll(getMiscInformationKpis());
		KpiStatistics.publishKpiEvents(kpiEvents);
	}

	private EventBuilder getEventBuilder(String destinationType)
	{
		EventBuilder eventBuilder = new EventBuilder()
				.setTimestamp(date.getTime())
				.setDomain(KpiStaticsConstants.BROKER_KPI_STATISTICS_DOMAIN)
				.addStringAttribute(KpiStaticsConstants.AGENT_NAME_ATTRIBUTE, GcsInfo.getAgentName());
		if (destinationType != null)
			eventBuilder.addStringAttribute(KpiStaticsConstants.DESTINATION_TYPE_ATTRIBUTE, destinationType);
		return eventBuilder;
	}

	private double getStatsValue(long value)
	{
		if (value == -1)
		{
			return 0.0;
		}
		return (double) value;
	}

	private double getStatsValue(long value, String name)
	{
		double doubleVal = getStatsValue(value);
		log.debug("Long value for metric {}: {}", name, value);
		log.debug("Double value for metric {}: {}", name, doubleVal);
		return doubleVal;
	}

	private List<Event> getQeueInfoKpis()
	{
		List<Event> kpis = Lists.newArrayList();
		for (QueueStatistics queueStats : statistics.getQueueStatistics())
		{
			EventBuilder eventBuilder = getEventBuilder(DestinationType.QUEUE.toString());
			eventBuilder.addStringAttribute(KpiStaticsConstants.QUEUE_NAME_ATTRIBUTE, queueStats.getQueueName());

			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_RECEIVED_METRIC, getStatsValue(queueStats.getReceivedMessages())));
			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_DELIVERED_METRIC, getStatsValue(queueStats.getDeliveredMessages())));
			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_EXPIRED_METRIC, getStatsValue(queueStats.getExpiredMessages())));
			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_REDELIVERED_METRIC, getStatsValue(queueStats.getRedeliveredMessages())));

			kpis.add(eventBuilder.buildMetricBundleEvent());
		}
		return kpis;
	}

	private List<Event> getTopicInfoKpis()
	{
		List<Event> kpis = Lists.newArrayList();
		for (TopicStatistics topicStats : statistics.getTopicStatistics())
		{
			EventBuilder eventBuilder = getEventBuilder(DestinationType.TOPIC.toString());
			eventBuilder.addStringAttribute(KpiStaticsConstants.TOPIC_NAME_ATTRIBUTE, topicStats.getTopicName());

			// TODO: Perguntar ao Neves porque é que não existem estatísticas de mensagens recebidas (input rate) por tópico
			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_DELIVERED_METRIC, getStatsValue(topicStats.getDeliveredMessages(), KpiStaticsConstants.MESSAGES_DELIVERED_METRIC)));
			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_DISCARDED_METRIC, getStatsValue(topicStats.getDiscardedMessages())));
			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_DISPATCHED_METRIC, getStatsValue(topicStats.getDispatchedToQueueMessages())));

			kpis.add(eventBuilder.buildMetricBundleEvent());
		}
		return kpis;
	}

	private List<Event> getChannelInfoKpis()
	{
		List<Event> kpis = Lists.newArrayList();
		for (ChannelStatistics channelStats : statistics.getChannelStatistics())
		{
			EventBuilder eventBuilder = getEventBuilder(channelStats.getChannelName());

			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_RECEIVED_METRIC, getStatsValue(channelStats.getReceivedMessages())));
			kpis.add(eventBuilder.buildMetricBundleEvent());
		}
		return kpis;
	}

	private List<Event> getMiscInformationKpis()
	{
		List<Event> kpis = Lists.newArrayList();
		SystemStatistics systemStats = statistics.getSystemStatistics();

		EventBuilder eventBuilder = getEventBuilder(null);
		eventBuilder.addStringAttribute("stats", "messages");
		eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_INVALID_METRIC, getStatsValue(systemStats.getInvalidMessages())));
		eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_ACCESS_DENIED_METRIC, getStatsValue(systemStats.getAccessDeniedMessages())));
		kpis.add(eventBuilder.buildMetricBundleEvent());

		eventBuilder = getEventBuilder(null);
		eventBuilder.addStringAttribute("stats", "connections");
		eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.CONNECTION_TCP_METRIC, getStatsValue(systemStats.getTcpConnections())));
		eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.CONNECTION_TCP_LEGACY_METRIC, getStatsValue(systemStats.getTcpLegacyConnections())));
		eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.CONNECTION_SSL_METRIC, getStatsValue(systemStats.getSslConnections())));
		kpis.add(eventBuilder.buildMetricBundleEvent());

		eventBuilder = getEventBuilder(null);
		eventBuilder.addStringAttribute("stats", "system");
		eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.MESSAGES_DELIVERY_FAILED_METRIC, getStatsValue(systemStats.getFailedMessages())));
		eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.SYSTEM_FAULTS_METRIC, getStatsValue(systemStats.getSystemFaults())));
		kpis.add(eventBuilder.buildMetricBundleEvent());

		return kpis;
	}

}
