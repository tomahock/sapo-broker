package pt.com.gcs.messaging;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.CriticalErrors;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetPublish;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.conf.GlobalConfig;

/**
 * GcsRemoteProtocolHandler is an NETTY SimpleChannelHandler. It handles outgoing connections to other agents (3315).
 * 
 */

@Sharable
class GcsRemoteProtocolHandler extends SimpleChannelHandler
{
	private static Logger log = LoggerFactory.getLogger(GcsRemoteProtocolHandler.class);
	private static final Charset UTF8 = Charset.forName("UTF-8");

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
		final NetMessage m = (NetMessage) e.getMessage();
		String mtype = m.getHeaders().get("TYPE");

		NetNotification nnot = m.getAction().getNotificationMessage();

		NetBrokerMessage brkMsg = nnot.getMessage();

		Channel channel = ctx.getChannel();

		if (log.isDebugEnabled())
		{
			log.debug("Message Received from: '{}', Type: '{}'", channel.getRemoteAddress(), mtype);
		}

		if (m.getHeaders() != null)
		{
			brkMsg.addAllHeaders(m.getHeaders());
		}
		brkMsg.addHeader("IS_REMOTE", "true");

		if (mtype.equals("COM_TOPIC"))
		{
			NetPublish np = new NetPublish(nnot.getDestination(), DestinationType.TOPIC, brkMsg);
			TopicProcessorList.notify(np, true);
		}
		else if (mtype.equals("COM_QUEUE"))
		{
			QueueProcessor queueProcessor = QueueProcessorList.get(nnot.getDestination());
			if (queueProcessor != null)
			{
				if (acknowledgeMessage(queueProcessor.getQueueName(), brkMsg.getMessageId(), channel))
				{
					queueProcessor.store(m, GlobalConfig.preferLocalConsumers());
				}
			}
		}
		else if (mtype.equals("SYSTEM_ACK"))
		{
			String msgContent = new String(brkMsg.getPayload(), UTF8);
			String messageId = extract(msgContent, "<message-id>", "</message-id>");
			SystemMessagesPublisher.messageAcknowledged(messageId);
		}
		else
		{
			log.warn("Unkwown message type. Don't know how to handle message. Type: '{}'", mtype);
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelClosed(ctx, e);

		Channel channel = ctx.getChannel();

		log.info("Session Closed: '{}'", channel.getRemoteAddress());

		Gcs.remoteSessionClosed(channel);

		if (OutboundRemoteChannels.remove(channel))
		{
			GcsExecutor.schedule(new Connect(channel.getRemoteAddress()), 5000, TimeUnit.MILLISECONDS);
		}
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
	{
		super.channelConnected(ctx, e);

		log.info("Session Opened: '{}'", ctx.getChannel().getRemoteAddress());
		sayHello(ctx);
	}

	private boolean acknowledgeMessage(String destination, String messageId, Channel channel)
	{
		log.debug("Acknowledge message with Id: '{}'.", messageId);

		if (!channel.isWritable())
		{
			log.warn("Can't acknowledge message because channel is not writable");
			return false;
		}

		try
		{
			NetBrokerMessage brkMsg = new NetBrokerMessage(new byte[0]);
			brkMsg.setMessageId(messageId);

			NetNotification notification = new NetNotification(destination, DestinationType.TOPIC, brkMsg, destination);

			NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
			naction.setNotificationMessage(notification);

			NetMessage nmsg = new NetMessage(naction);
			nmsg.getHeaders().put("TYPE", "ACK");

			// channel is writable. checked before
			channel.write(nmsg);
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
			return false;
		}
		return true;
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
			NetBrokerMessage brkMsg = new NetBrokerMessage(agentId.getBytes(UTF8));

			NetNotification notification = new NetNotification("/system/peer", DestinationType.TOPIC, brkMsg, "/system/peer");

			NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
			naction.setNotificationMessage(notification);

			NetMessage nmsg = new NetMessage(naction);
			nmsg.getHeaders().put("TYPE", "HELLO");

			log.info("Send agentId: '{}'", agentId);

			channel.write(nmsg);
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
