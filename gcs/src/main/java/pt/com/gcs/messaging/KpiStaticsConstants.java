package pt.com.gcs.messaging;

public final class KpiStaticsConstants {
	
	public static final String BROKER_KPI_STATISTICS_DOMAIN = "broker.bk.sapo.pt";
	/**
	 * KPI name attributes:
	 * */
	public static final String AGENT_NAME_ATTRIBUTE = "agent-name";
	public static final String DESTINATION_TYPE_ATTRIBUTE = "destination-type";
	public static final String QUEUE_NAME_ATTRIBUTE = "queue-name";
	public static final String TOPIC_NAME_ATTRIBUTE = "topic-name";
	public static final String INPUT_RATE_ATTRIBUTE = "input-rate";
	public static final String OUTPUT_RATE_ATTRIBUTE = "output-rate";
	public static final String EXPIRED_RATE_ATTRIBUTE = "expired-rate";
	public static final String REDELIVERED_RATE_ATTRIBUTE = "redelivered-rate";
	public static final String DISCARDED_RATE_ATTRIBUTE = "discarded-rate";
	public static final String DISPATCHED_TO_QUEUE_ATTRIBUTE = "dispatched-to-queue-rate";
	public static final String INVALID_MESSAGES_INPUT_RATE_ATTRIBUTE = "invalid-messages-input-rate";
	public static final String ACCESS_DENIED_ATTRIBUTE = "access-denied";
	public static final String SYSTEM_MESSAGE_FAILED_DELIVERY = "system-message-delivery-failed";
	public static final String TCP_CONNECTIONS_ATTRIBUTE = "tcp";
	public static final String TCP_LEGACY_CONNECTIONS_ATTRIBUTE = "tcp-legacy";
	public static final String SSL_CONNECTIONS_ATTRIBUTE = "ssl";
	
	
	/**
	 * Metrics to collect
	 * */
	public static final String MESSAGES_RECEIVED_METRIC = "messages-received";
	public static final String MESSAGES_DELIVERED_METRIC = "messages-delivered";
	public static final String MESSAGES_EXPIRED_METRIC = "messages-expired";
	public static final String MESSAGES_REDELIVERED_METRIC = "messages-redelivered";
	public static final String MESSAGES_DISCARDED_METRIC = "messages-discarded";
	public static final String MESSAGES_DISPATCHED_METRIC = "messages-dispatched";
	public static final String MESSAGES_INVALID_METRIC = "messages-invalid";
	public static final String MESSAGES_ACCESS_DENIED_METRIC = "messages-access-denied";
	public static final String MESSAGES_DELIVERY_FAILED_METRIC = "messages-delivery-failed";
	public static final String CONNECTION_TCP_METRIC = "tcp";
	public static final String CONNECTION_TCP_LEGACY_METRIC = "tcp";
	public static final String CONNECTION_SSL_METRIC = "ssl";
	public static final String SYSTEM_FAULTS_METRIC = "faults";

}
