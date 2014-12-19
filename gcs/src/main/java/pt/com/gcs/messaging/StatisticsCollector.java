package pt.com.gcs.messaging;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import pt.com.broker.types.stats.ChannelStats;
import pt.com.broker.types.stats.MiscStats;
import pt.com.gcs.stats.ChannelStatistics;
import pt.com.gcs.stats.QueueStatistics;
import pt.com.gcs.stats.Statistics;
import pt.com.gcs.stats.SystemStatistics;
import pt.com.gcs.stats.TopicStatistics;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class StatisticsCollector implements Runnable {
	
	private static final Logger log = LoggerFactory.getLogger(StatisticsCollector.class);
	
	private static volatile Date date = new Date();
	
	@Override
	public void run() {
		try{
			Date oldDate = date;
			date = new Date();
			Statistics stats = getStatistics();
			KpiGlobalStatisticsPublisher kpiStatsPublisher = new KpiGlobalStatisticsPublisher(date, stats);
			GlobalStatisticsPublisher globalStatsPublisher = new GlobalStatisticsPublisher(oldDate, date, stats);
			kpiStatsPublisher.publishStatistics();
			globalStatsPublisher.publishStatistics();
		} catch(Exception e){
			log.error("Unhandled exception caught collecting agent statistics.", e);
		}
	}
	
	/**
	 * Collects and resets the statistics counter. The counter values for the different statistics available
	 * are placed on a hashmap and are passed to specific formater classes that serialize the values in the
	 * correct format and sends them to the right place.
	 * */
	private Statistics getStatistics(){
		Statistics statistics = new Statistics();
		getQueueStats(statistics);
		getTopicStats(statistics);
		getChannelStats(statistics);
		getSystemStats(statistics);
		return statistics;
	}
	
	private void getQueueStats(Statistics statistics){
		List<QueueStatistics> queueStatistics = Lists.newArrayList();
		for (QueueProcessor qp: QueueProcessorList.values())
		{
			QueueStatistics queueStats = new QueueStatistics();
			queueStats.setQueueName(qp.getQueueName());
			queueStats.setReceivedMessages(qp.getQueueStatistics().getQueueMessagesReceivedAndReset());
			queueStats.setDeliveredMessages(qp.getQueueStatistics().getQueueMessagesDeliveredAndReset());
			queueStats.setExpiredMessages(qp.getQueueStatistics().getQueueMessagesExpiredAndReset());
			queueStats.setRedeliveredMessages(qp.getQueueStatistics().getQueueMessagesRedeliveredAndReset());
			queueStatistics.add(queueStats);
		}
		statistics.setQueueStatistics(queueStatistics);
	}
	
	private void getTopicStats(Statistics statistics){
		List<TopicStatistics> topicStatistics = Lists.newArrayList();
		for(TopicProcessor tp: TopicProcessorList.values()){
			TopicStatistics topicStats = new TopicStatistics();
			topicStats.setTopicName(tp.getSubscriptionName());
			topicStats.setDeliveredMessages(tp.getTopicStatistics().getTopicMessagesDeliveredAndReset());
			topicStats.setDiscardedMessages(tp.getTopicStatistics().getTopicMessagesDiscardedAndReset());
			topicStats.setDispatchedToQueueMessages(tp.getTopicStatistics().getTopicMessagesDispatchedToQueueAndReset());
			topicStatistics.add(topicStats);
		}
		statistics.setTopicStatistics(topicStatistics);
	}
	
	//TODO: Perguntar ao Neves se quer enviar também as estatísticas de HTTP.
	private void getChannelStats(Statistics statistics){
		List<ChannelStatistics> channelStatistics = Lists.newArrayList();
		ChannelStatistics channelStats = new ChannelStatistics();
		channelStats.setChannelName("dropbox");
		channelStats.setReceivedMessages(ChannelStats.getDropboxReceivedMessagesAndReset());
		channelStatistics.add(channelStats);
		
		statistics.setChannelStatistics(channelStatistics);
	}
	
	private void getSystemStats(Statistics statistics){
		SystemStatistics systemStats = new SystemStatistics();
		systemStats.setInvalidMessages(MiscStats.getInvalidMessagesAndReset());
		systemStats.setAccessDeniedMessages(MiscStats.getAccessesDeniedAndReset());
		systemStats.setTcpConnections(MiscStats.getTcpConnections());
		systemStats.setTcpLegacyConnections(MiscStats.getTcpLegacyConnections());
		systemStats.setSslConnections(MiscStats.getSslConnections());
		systemStats.setFailedMessages(MiscStats.getSystemMessagesFailuresAndReset());
		systemStats.setSystemFaults(MiscStats.getFaultsAndReset());
		systemStats.setReceivedTopicMessages(TopicProcessorList.getTopicMessagesReceivedAndReset());
		statistics.setSystemStatistics(systemStats);
	}

}
