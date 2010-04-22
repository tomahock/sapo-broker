package pt.com.broker.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.Headers;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;

/**
 * BrokerProducer handles message publication both topic and queues.
 */
public class BrokerProducer
{
	private static final Logger log = LoggerFactory.getLogger(BrokerProducer.class);

	private static final BrokerProducer instance = new BrokerProducer();

	public static BrokerProducer getInstance()
	{
		return instance;
	}

	private BrokerProducer()
	{
	}

	public boolean enqueueMessage(final NetPublish np, String messageSource)
	{
		StringBuilder sb_source = new StringBuilder();
		sb_source.append("queue@");
		sb_source.append(GcsInfo.getAgentName());
		sb_source.append("://");
		sb_source.append(np.getDestination());
		sb_source.append("?app=");
		sb_source.append(messageSource);

		np.getMessage().addHeader(Headers.FROM, sb_source.toString());

		NetMessage nmsg = Gcs.buildNotification(np, np.getDestination());

		return Gcs.enqueue(nmsg, np.getDestination());
	}

	public void publishMessage(final NetPublish np, final String messageSource)
	{
		StringBuilder sb_source = new StringBuilder();
		sb_source.append("topic@");
		sb_source.append(GcsInfo.getAgentName());
		sb_source.append("://");
		sb_source.append(np.getDestination());
		sb_source.append("?app=");
		sb_source.append(messageSource);

		np.getMessage().addHeader(Headers.FROM, sb_source.toString());

		Gcs.publish(np);
	}
}
