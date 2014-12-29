package pt.com.gcs.messaging.statistics;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.caudexorigo.time.ISO8601;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.MessageListener;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalPublisher;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;
import pt.com.gcs.messaging.TopicProcessor;
import pt.com.gcs.messaging.TopicProcessorList;
import pt.sapo.socialbus.common.kpi.EventBuilder;
import pt.sapo.socialbus.common.kpi.data.Event;
import pt.sapo.socialbus.common.kpi.data.MetricItem;
import pt.sapo.socialbus.common.kpi.data.MetricType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public abstract class KpiStatistics {
	
	private static final Logger log = LoggerFactory.getLogger(KpiStatistics.class);
	
	public static final void publishKpiEvents(List<Event> events){
		log.debug("Publishing #{} KpiEvents.", events.size());
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		for(Event event: events){
			//FIXME: Get a way to indicate the caller that the message encoding generated an exception
			try {
				//FIXME: Remove the hardcoded queue from here.
				NetPublish np = new NetPublish("/sapo/event-agg-kpi", DestinationType.TOPIC, new NetBrokerMessage(objectMapper.writeValueAsString(event)));
				Gcs.publish(np);
			} catch (JsonProcessingException e) {
				log.error("Error encoding event object to json. Event object: {}", event.toString(), e);
			}
		}
	}
	
	private static final EventBuilder getEventBuilder(String destinationType){
		EventBuilder eventBuilder = new EventBuilder()
			.setTimestamp(new Date().getTime())
			.setDomain(KpiStaticsConstants.BROKER_KPI_STATISTICS_DOMAIN)
			.addStringAttribute(KpiStaticsConstants.AGENT_NAME_ATTRIBUTE, GcsInfo.getAgentName());
		if(destinationType != null)
			eventBuilder.addStringAttribute(KpiStaticsConstants.DESTINATION_TYPE_ATTRIBUTE, destinationType);
	return eventBuilder;
	}
	
	public static final List<Event> getKpiTopicListeners(){
		List<Event> events = Lists.newArrayList();
		for(TopicProcessor tp: TopicProcessorList.values()){
			EventBuilder eventBuilder = getEventBuilder(DestinationType.TOPIC.toString());
			int localListeners = 0;
			for(MessageListener listener: tp.listeners()){
				if(listener.getType() == MessageListener.Type.LOCAL){
					localListeners++;
				}
			}
			if(localListeners > 0){
				eventBuilder.addStringAttribute(KpiStaticsConstants.TOPIC_NAME_ATTRIBUTE, tp.getSubscriptionName());
				eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.SUBSCRIPTIONS_METRIC, (double) localListeners));
				events.add(eventBuilder.buildMetricBundleEvent());
			}
		}
		return events;
	}
	
	public static final List<Event> getKpiQueueListeners(){
		List<Event> events = Lists.newArrayList();
		for(QueueProcessor qp: QueueProcessorList.values()){
			EventBuilder eventBuilder = getEventBuilder(DestinationType.QUEUE.toString());
			if(qp.localListeners().size() > 0){
				eventBuilder.addStringAttribute(KpiStaticsConstants.QUEUE_NAME_ATTRIBUTE, qp.getQueueName());
				eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.SUBSCRIPTIONS_METRIC, (double) qp.localListeners().size()));
				events.add(eventBuilder.buildMetricBundleEvent());
			}
		}
		return events;
	}
	
	/**
	 * Old statistics to send periodically the registered queue names.
	 * Unused method at the moment.
	 * */
	public static final List<Event> getQueuesList(){
		List<Event> events = Lists.newArrayList();	
		return events;
	}
	
	public static final List<Event> getQueuesSize(){
		List<Event> events = Lists.newArrayList();
		for(QueueProcessor qp: QueueProcessorList.values()){
			EventBuilder eventBuilder = getEventBuilder(DestinationType.QUEUE.toString());
			eventBuilder.addStringAttribute(KpiStaticsConstants.QUEUE_NAME_ATTRIBUTE, qp.getQueueName());
			eventBuilder.addMetricItem(new MetricItem(MetricType.counter, KpiStaticsConstants.QUEUED_MESSAGES_METRIC, (double) qp.getQueuedMessagesCount()));
			events.add(eventBuilder.buildMetricBundleEvent());
		}
		return events;
	}
	
}
