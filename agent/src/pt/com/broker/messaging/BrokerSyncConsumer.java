package pt.com.broker.messaging;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.channels.ChannelAttributes;
import pt.com.broker.types.channels.ListenerChannel;
import pt.com.broker.types.channels.ListenerChannelFactory;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;

/**
 * BrokerSyncConsumer represents a queue synchronous consumer.
 */
public class BrokerSyncConsumer
{
	private static final Logger log = LoggerFactory.getLogger(BrokerSyncConsumer.class);

	private static final String SESSION_ATT_PREFIX = "SYNC_MESSAGE_LISTENER#";
	private static final AtomicInteger zeroValue = new AtomicInteger(0);

	private static ConcurrentMap<String, AtomicInteger> synConsumersCount = new ConcurrentHashMap<String, AtomicInteger>();

	public static void removeSession(ChannelHandlerContext ctx)
	{
		// Set<String> attributeKeys = channel.getAttributeKeys();
		// Channel channel = ctx.getChannel();
		Set<String> attributeKeys = ChannelAttributes.getAttributeKeys(ChannelAttributes.getChannelId(ctx));
		for (String attributeKey : attributeKeys)
		{
			if (attributeKey.toString().startsWith(SESSION_ATT_PREFIX))
			{
				Object attributeValue = ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), attributeKey);
				if (attributeValue instanceof SynchronousMessageListener)
				{
					SynchronousMessageListener listener = (SynchronousMessageListener) attributeValue;
					pollStoped(listener.getsubscriptionKey());
					QueueProcessorList.removeListener(listener);
				}
			}
		}
	}

	public static void poll(NetPoll poll, ChannelHandlerContext ctx, String reserveArgument)
	{
		Channel channel = ctx.getChannel();
		try
		{
			String queueName = poll.getDestination();

			if (StringUtils.isBlank(queueName))
			{
				String error = "Can't poll a message from a queue whose name is blank.";
				log.error(error);
				throw new RuntimeException(error);
			}

			QueueProcessorList.get(queueName);

			String composedQueueName = SESSION_ATT_PREFIX + queueName;
			Object attribute = ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), composedQueueName);

			SynchronousMessageListener msgListener = null;
			if (attribute != null)
			{
				msgListener = (SynchronousMessageListener) attribute;
			}
			else
			{
				ListenerChannel lchannel = ListenerChannelFactory.getListenerChannel(ctx);

				msgListener = new SynchronousMessageListener(lchannel, queueName);

				ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), composedQueueName, msgListener);
				QueueProcessor queueProcessor = QueueProcessorList.get(queueName);
				if (queueProcessor != null)
				{
					queueProcessor.add(msgListener);
				}
				AtomicInteger previous = synConsumersCount.putIfAbsent(poll.getDestination(), new AtomicInteger(1));
				if (previous != null)
				{
					previous.incrementAndGet();
				}
			}

			long reserveTime = getReserveTime(reserveArgument);
			if (reserveTime != -1)
			{
				msgListener.setReserveTime(reserveTime);
			}
			msgListener.activate(poll.getTimeout(), poll.getActionId());
		}
		catch (Throwable t)
		{
			try
			{
				((BrokerProtocolHandler) ctx.getHandler()).exceptionCaught(ctx, t, null);
			}
			catch (Throwable t2)
			{
				throw new RuntimeException(t2);
			}
		}
	}

	private static void pollStoped(String destination)
	{
		AtomicInteger count = synConsumersCount.get(destination);
		if (count != null)
		{
			count.decrementAndGet();
			synConsumersCount.remove(destination, zeroValue); // remove if zero
		}
	}

	private static long getReserveTime(String reserveArgument)
	{
		long reserveTime = -1;
		if (StringUtils.isNotBlank(reserveArgument))
		{
			try
			{
				reserveTime = Long.parseLong(reserveArgument);
			}
			catch (NumberFormatException nfe)
			{
				log.error("");
			}
		}
		return reserveTime;
	}

}