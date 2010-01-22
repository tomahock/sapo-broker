package pt.com.gcs.messaging;

import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.net.IoSessionHelper;

/**
 * GcsAcceptorProtocolHandler is an MINA IoHandlerAdapter. It handles incoming messages, such as publications, from other agents.
 * 
 */

class GcsRemoteProtocolHandler extends IoHandlerAdapter
{
	private static Logger log = LoggerFactory.getLogger(GcsRemoteProtocolHandler.class);

	@Override
	public void exceptionCaught(IoSession iosession, Throwable cause) throws Exception
	{
		Throwable rootCause = ErrorAnalyser.findRootCause(cause);
		CriticalErrors.exitIfCritical(rootCause);
		log.error("Exception Caught:{}, {}", IoSessionHelper.getRemoteAddress(iosession), rootCause.getMessage());
		if (iosession.isConnected() && !iosession.isClosing())
		{
			log.error("STACKTRACE", rootCause);
		}

		try
		{
			iosession.close();
		}
		catch (Throwable t)
		{
			log.error("STACKTRACE", t);
		}
		
	}

	@Override
	public void messageReceived(final IoSession iosession, Object omessage) throws Exception
	{
		final InternalMessage msg = (InternalMessage) omessage;

		if (log.isDebugEnabled())
		{
			log.debug("Message Received from: '{}', Type: '{}'", IoSessionHelper.getRemoteAddress(iosession), msg.getType());
		}

		msg.setFromRemotePeer(true);

		switch (msg.getType())
		{
		case COM_TOPIC:
			LocalTopicConsumers.notify(msg);
			break;
		case COM_QUEUE:
			QueueProcessorList.get(msg.getDestination()).store(msg, true);
			LocalQueueConsumers.acknowledgeMessage(msg, iosession);
			break;
		case SYSTEM_ACK:
		{
			String msgContent = new String(msg.getContent().getPayload(), "UTF-8");
			String messageId = extract(msgContent, "<message-id>", "</message-id>");
			SystemMessagesPublisher.messageAcknowledged(messageId);
		}
			break;
		default:
			log.warn("Unkwown message type. Don't know how to handle message");
		}
	}

	@Override
	public void messageSent(IoSession iosession, Object message) throws Exception
	{
		if (log.isDebugEnabled())
		{
			log.debug("Message Sent: '{}', '{}'", IoSessionHelper.getRemoteAddress(iosession), message.toString());
		}
	}

	@Override
	public void sessionClosed(final IoSession iosession) throws Exception
	{
		log.info("Session Closed: '{}'", IoSessionHelper.getRemoteAddress(iosession));
		
		SystemMessagesPublisher.sessionClosed(iosession);
		GcsExecutor.schedule(new Connect((SocketAddress) IoSessionHelper.getRemoteInetAddress(iosession)), 5000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void sessionCreated(IoSession iosession) throws Exception
	{
		IoSessionHelper.tagWithRemoteAddress(iosession);
		if (log.isDebugEnabled())
		{
			log.debug("Session Created: '{}'", IoSessionHelper.getRemoteAddress(iosession));
		}
	}

	@Override
	public void sessionIdle(IoSession iosession, IdleStatus status) throws Exception
	{
		if (log.isDebugEnabled())
		{
			log.debug("Session Idle:'{}'", IoSessionHelper.getRemoteAddress(iosession));
		}
	}

	@Override
	public void sessionOpened(IoSession iosession) throws Exception
	{
		log.info("Session Opened: '{}'", IoSessionHelper.getRemoteAddress(iosession));
		sayHello(iosession);
	}

	public void sayHello(IoSession iosession)
	{
		if (log.isDebugEnabled())
		{
			log.debug("Say Hello: '{}'", IoSessionHelper.getRemoteAddress(iosession));
		}

		try
		{
			String agentId = GcsInfo.getAgentName() + "@" + GcsInfo.getAgentHost() + ":" + GcsInfo.getAgentPort();
			NetBrokerMessage brkMsg = new NetBrokerMessage(agentId.getBytes("UTF-8"));

			InternalMessage m = new InternalMessage();
			m.setType((MessageType.HELLO));
			m.setDestination("HELLO");
			m.setContent(brkMsg);

			log.info("Send agentId: '{}'", agentId);

			iosession.write(m);
		}
		catch (Throwable t)
		{
			try
			{
				iosession.close();
			}
			catch (Throwable ict)
			{
				log.error(ict.getMessage(), ict);
			}
			return;
		}

		Set<String> topicNameSet = LocalTopicConsumers.getBroadcastableTopics();
		for (String topicName : topicNameSet)
		{
			LocalTopicConsumers.broadCastTopicInfo(topicName, "CREATE", iosession);
		}

		Set<String> queueNameSet = LocalQueueConsumers.getBroadcastableQueues();
		for (String queueName : queueNameSet)
		{
			try
			{
				LocalQueueConsumers.broadCastQueueInfo(queueName, "CREATE", iosession);
			}
			catch (Throwable t)
			{
				try
				{
					iosession.close();
				}
				catch (Throwable ict)
				{
					log.error(ict.getMessage(), ict);
				}
			}
		}
	}

	private String extract(String ins, String prefix, String sufix)
	{
		if (StringUtils.isBlank(ins))
		{
			return "";
		}

		int s = ins.indexOf(prefix) + prefix.length();
		int e = ins.indexOf(sufix);
		return ins.substring(s, e);
	}
}
