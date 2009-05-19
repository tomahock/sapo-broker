package pt.com.broker.messaging;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAcknowledgeMessage;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPublish;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.MessageType;

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

	private InternalMessage prepareForSending(NetPublish publish)
	{
		try
		{
			NetBrokerMessage brkMessage = publish.getMessage();

			final InternalMessage message = new InternalMessage();

			if (StringUtils.isNotBlank(brkMessage.getMessageId()))
				message.setMessageId(brkMessage.getMessageId());

			if (StringUtils.isNotBlank(publish.getDestination()))
			{
				message.setDestination(publish.getDestination());
				message.setPublishDestination(publish.getDestination());
			}
			if (brkMessage.getTimestamp() != -1)
				message.setTimestamp(brkMessage.getTimestamp());

			if (brkMessage.getExpiration() != -1)
			{
				message.setExpiration(brkMessage.getExpiration());
				System.out.println("BrokerProducer.prepareForSending() - setExpiration");
			}

			message.setContent(brkMessage);

			if (log.isDebugEnabled())
			{
				log.debug("Received message: {}", message.getMessageId());
			}

			return message;
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public void enqueueMessage(final NetPublish enqReq, String messageSource)
	{
		InternalMessage msg = prepareForSending(enqReq);

		StringBuffer sb_source = new StringBuffer();
		sb_source.append("queue@");
		sb_source.append(GcsInfo.getAgentName());
		sb_source.append("://");
		sb_source.append(enqReq.getDestination());
		if (StringUtils.isNotBlank(messageSource))
		{
			sb_source.append("?app=");
			sb_source.append(messageSource);
		}
		msg.setSourceApp(sb_source.toString());
		msg.setType(MessageType.COM_QUEUE);

		Gcs.enqueue(msg);
	}

	public void publishMessage(final NetPublish pubReq, final String messageSource)
	{
		InternalMessage msg = prepareForSending(pubReq);

		StringBuffer sb_source = new StringBuffer();
		sb_source.append("topic@");
		sb_source.append(GcsInfo.getAgentName());
		sb_source.append("://");
		sb_source.append(pubReq.getDestination());
		if (StringUtils.isNotBlank(messageSource))
		{
			sb_source.append("?app=");
			sb_source.append(messageSource);
		}
		msg.setSourceApp(sb_source.toString());
		msg.setType(MessageType.COM_TOPIC);
		Gcs.publish(msg);
	}

	public void acknowledge(NetAcknowledgeMessage ackReq)
	{
		Gcs.ackMessage(ackReq.getDestination(), ackReq.getMessageId());
	}
}
