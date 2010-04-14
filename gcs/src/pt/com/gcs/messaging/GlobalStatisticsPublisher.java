package pt.com.gcs.messaging;

import java.util.Date;

import org.caudexorigo.text.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.stats.ChannelStats;
import pt.com.broker.types.stats.EncodingStats;
import pt.com.broker.types.stats.MiscStats;
import pt.com.gcs.conf.GcsInfo;

public class GlobalStatisticsPublisher implements Runnable
{
	private static Logger log = LoggerFactory.getLogger(GlobalStatisticsPublisher.class);
	
	private static Date date = new Date();
	
	
	@Override
	public void run()
	{
		Date oldDate = date;
		date = new Date();
		
		long difSeconds = (date.getTime() - oldDate.getTime()) / 1000;
		
		
		String currentDateStr =  DateUtil.formatISODate(date);
		
		publishQueueInfo(currentDateStr, difSeconds);
		
		publishTopicInfo(currentDateStr, difSeconds);
		
		publishChannelInfo(currentDateStr, difSeconds);
		
		publishEncodingInfo(currentDateStr, difSeconds);
		
		publishMiscInformation(currentDateStr, difSeconds);
	}


	private void publishQueueInfo(String date, long seconds)
	{
		double dSeconds = (double) seconds;
		
		final String topic = "/system/stats/queues/";
		
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("<mqinfo date=\"%s\" agent-name=\"%s\">", date, GcsInfo.getAgentName()));
		
		double rate;
		for(QueueProcessor qp : QueueProcessorList.values() )
		{
			rate = ((double)qp.getQueueMessagesReceivedAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"input-rate\" value=\"%s\" />", qp.getQueueName(), rate ));
			rate = ((double)qp.getQueueMessagesDeliveredAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"output-rate\" value=\"%s\" />", qp.getQueueName(), rate));
			rate = ((double)qp.getQueueMessagesFailedAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"failed-rate\" value=\"%s\" />", qp.getQueueName(), rate));
			rate = ((double)qp.getQueueMessagesExpiredAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"expired-rate\" value=\"%s\" />", qp.getQueueName(), rate));
			rate = ((double)qp.getQueueMessagesRedeliveredAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"queue://%s\" predicate=\"redelivered-rate\" value=\"%s\" />", qp.getQueueName(), rate));
		}

		sb.append("\n</mqinfo>");
		
		String result = sb.toString();
		log.info('\n' + result);

		NetPublish np = new NetPublish(String.format("%s#%s#", topic, GcsInfo.getAgentName()), DestinationType.TOPIC, new NetBrokerMessage(result));
		
		Gcs.publish(np);
	}

	private void publishTopicInfo(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();
		final String topic = "/system/stats/topics/";
		
		
		sb.append(String.format("<mqinfo date=\"%s\" agent-name=\"%s\">", date, GcsInfo.getAgentName()));
		
		double rate;
		rate = ((double) TopicProcessorList.getTopicMessagesReceivedAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"topic://.*\" predicate=\"input-rate\" value=\"%s\" />", rate));
		for(TopicProcessor tp : TopicProcessorList.values() )
		{
			rate = ((double) tp.getTopicMessagesDeliveredAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"output-rate\" value=\"%s\" />",tp.getSubscriptionName(), rate));
			rate = ((double)  tp.getTopicMessagesDiscardedAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"discarded-rate\" value=\"%s\" />",tp.getSubscriptionName(), rate));
			rate = ((double) tp.getTopicMessagesDispatchedToQueueAndReset()/dSeconds);
			sb.append(String.format("\n	<item subject=\"topic://%s\" predicate=\"dispatched-to-queue-rate\" value=\"%s\" />",tp.getSubscriptionName(), rate));
		}

		sb.append("\n</mqinfo>");

		String result = sb.toString();
		log.info('\n' + result);

		NetPublish np = new NetPublish(String.format("%s#%s#", topic, GcsInfo.getAgentName()), DestinationType.TOPIC, new NetBrokerMessage(result));
		
		Gcs.publish(np);
	}
	
	private void publishChannelInfo(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();
		final String topic = "/system/stats/channels/";
		
		
		sb.append(String.format("<mqinfo date=\"%s\" agent-name=\"%s\">", date, GcsInfo.getAgentName()));
		
		double rate;
		rate = ((double) ChannelStats.getDropboxReceivedMessagesAndReset() /dSeconds);
		sb.append(String.format("\n	<item subject=\"dropbox\" predicate=\"input-rate\" value=\"%s\" />", rate));
		rate = ((double) ChannelStats.getHttpReceivedMessagesAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"http\" predicate=\"input-rate\" value=\"%s\" />", rate));
		
		sb.append("\n</mqinfo>");

		String result = sb.toString();
		log.info('\n' + result);
		
		NetPublish np = new NetPublish(String.format("%s#%s#", topic, GcsInfo.getAgentName()), DestinationType.TOPIC, new NetBrokerMessage(result));
		
		Gcs.publish(np);
	}
	
	private void publishEncodingInfo(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();
		final String topic = "/system/stats/encoding/";
		
		
		sb.append(String.format("<mqinfo date=\"%s\" agent-name=\"%s\">", date, GcsInfo.getAgentName()));
		
		double rate;
		rate = ((double) EncodingStats.getSoapDecodedMessageAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"xml\" predicate=\"input-rate\" value=\"%s\" />", rate));
		rate = ((double) EncodingStats.getSoapEncodedMessageAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"xml\" predicate=\"output-rate\" value=\"%s\" />", rate));
		
		rate = ((double) EncodingStats.getProtoDecodedMessageAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"protobuf\" predicate=\"input-rate\" value=\"%s\" />", rate));
		rate = ((double) EncodingStats.getProtoEncodedMessageAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"protobuf\" predicate=\"output-rate\" value=\"%s\" />", rate));
		
		rate = ((double) EncodingStats.getThriftDecodedMessageAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"thrift\" predicate=\"input-rate\" value=\"%s\" />", rate));
		rate = ((double) EncodingStats.getThriftEncodedMessageAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"thrift\" predicate=\"output-rate\" value=\"%s\" />", rate));
		
		sb.append("\n</mqinfo>");
		
		String result = sb.toString();
		log.info('\n' + result);

		NetPublish np = new NetPublish(String.format("%s#%s#", topic, GcsInfo.getAgentName()), DestinationType.TOPIC, new NetBrokerMessage(result));
		
		Gcs.publish(np);
	}

	private void publishMiscInformation(String date, long seconds)
	{
		double dSeconds = (double) seconds;

		StringBuilder sb = new StringBuilder();
		final String topic = "/system/stats/misc/";
		
		sb.append(String.format("<mqinfo date=\"%s\" agent-name=\"%s\">", date, GcsInfo.getAgentName()));
		
		double rate;
		// invalid messages
		rate = ((double) MiscStats.getInvalidMessagesAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"invalid-messages\" predicate=\"input-rate\" value=\"%s\" />", rate));
		// access denied
		rate = ((double) MiscStats.getAccessesDeniedAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"access\" predicate=\"denied\" value=\"%s\" />", rate));
		// tcp, tcp-legacy, ssl
		sb.append(String.format("\n	<item subject=\"tcp\" predicate=\"connections\" value=\"%s\" />", MiscStats.getTcpConnectionsAndReset()));
		sb.append(String.format("\n	<item subject=\"tcp-legacy\" predicate=\"connections\" value=\"%s\" />", MiscStats.getTcpLegacyConnectionsAndReset()));
		sb.append(String.format("\n	<item subject=\"ssl\" predicate=\"connections\" value=\"%s\" />", MiscStats.getSslConnectionsAndReset()));
		// pending system message ack (count)
		sb.append(String.format("\n	<item subject=\"system-message\" predicate=\"ack-pending\" value=\"%s\" />", SystemMessagesPublisher.getPendingMessagesCount()));
		// System messages - failed delivery (count)
		sb.append(String.format("\n	<item subject=\"system-message\" predicate=\"failed-delivery\" value=\"%s\" />", MiscStats.getSystemMessagesFailuresAndReset()));
		// faults (rate)
		rate = ((double) MiscStats.getFaultsAndReset()/dSeconds);
		sb.append(String.format("\n	<item subject=\"faults\" predicate=\"rate\" value=\"%s\" />", rate));
		
		sb.append("\n</mqinfo>");

		String result = sb.toString();
		log.info('\n' + result);
		
		NetPublish np = new NetPublish(String.format("%s#%s#", topic, GcsInfo.getAgentName()), DestinationType.TOPIC, new NetBrokerMessage(result));
		
		Gcs.publish(np);	
	}

}
