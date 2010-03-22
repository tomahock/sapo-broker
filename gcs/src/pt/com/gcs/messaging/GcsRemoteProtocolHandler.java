package pt.com.gcs.messaging;

import java.util.concurrent.TimeUnit;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.gcs.conf.GcsInfo;

/**
 * GcsAcceptorProtocolHandler is an NETTY SimpleChannelHandler. It handles incoming messages, such as publications, from other agents.
 * 
 */

@Sharable
class GcsRemoteProtocolHandler extends SimpleChannelHandler
{
	private static Logger log = LoggerFactory.getLogger(GcsRemoteProtocolHandler.class);

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
	{
		Channel channel = ctx.getChannel();

		Throwable rootCause = ErrorAnalyser.findRootCause(e.getCause());
		CriticalErrors.exitIfCritical(rootCause);
		log.error("Exception Caught:{}, {}", channel.getRemoteAddress(), rootCause.getMessage());
		if (channel.isConnected())
		{
			log.error("STACKTRACE", rootCause);
		}

		try
		{
			channel.close();
		}
		catch (Throwable t)
		{
			log.error("STACKTRACE", t);
		}

	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
	{
		final InternalMessage msg = (InternalMessage) e.getMessage();

		Channel channel = ctx.getChannel();

		if (log.isDebugEnabled())
		{
			log.debug("Message Received from: '{}', Type: '{}'", channel.getRemoteAddress(), msg.getType());
		}

		msg.setFromRemotePeer(true);

		switch (msg.getType())
		{
		case COM_TOPIC:
			TopicProcessorList.notify(msg, true);
			break;
		case COM_QUEUE:
			QueueProcessor queueProcessor = QueueProcessorList.get(msg.getDestination());
			if(queueProcessor != null)
			{
				queueProcessor.store(msg, true);
				acknowledgeMessage(msg, channel);
			}
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
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelClosed(ctx, e);

		Channel channel = ctx.getChannel();

		log.info("Session Closed: '{}'", channel.getRemoteAddress());

		SystemMessagesPublisher.sessionClosed(channel);
		Gcs.remoteSessionClosed(channel);
		GcsExecutor.schedule(new Connect(channel.getRemoteAddress()), 5000, TimeUnit.MILLISECONDS);

	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelConnected(ctx, e);

		log.info("Session Opened: '{}'", ctx.getChannel().getRemoteAddress());
		sayHello(ctx);
	}

	private void acknowledgeMessage(InternalMessage msg, Channel channel)
	{
		log.debug("Acknowledge message with Id: '{}'.", msg.getMessageId());

		try
		{
			NetBrokerMessage brkMsg = new NetBrokerMessage("ACK".getBytes("UTF-8"));

			InternalMessage m = new InternalMessage(msg.getMessageId(), msg.getDestination(), brkMsg);
			m.setType(MessageType.ACK);

			channel.write(m);
		}
		catch (Throwable ct)
		{
			log.error(ct.getMessage(), ct);

			try
			{
				channel.close();
			}
			catch (Throwable ict)
			{
				log.error(ict.getMessage(), ict);
			}
		}
	}

	public void sayHello(ChannelHandlerContext ctx)
	{
		Channel channel = ctx.getChannel();

		if (log.isDebugEnabled())
		{
			log.debug("Say Hello: '{}'", channel.getRemoteAddress());
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

			channel.write(m);
		}
		catch (Throwable t)
		{
			try
			{
				channel.close();
			}
			catch (Throwable ict)
			{
				log.error(ict.getMessage(), ict);
			}
			return;
		}

		TopicProcessorList.broadcast("CREATE", channel);

		QueueProcessorList.broadcast("CREATE", channel);
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
