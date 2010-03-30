package pt.com.broker.messaging;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.text.StringUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.ChannelAttributes;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPoll;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalPublisher;
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

	static
	{
		Runnable counter = new Runnable()
		{
			public void run()
			{
				try
				{
					Collection<String> synConsumersList = synConsumersCount.keySet();

					for (String queueName : synConsumersList)
					{
						String ctName = String.format("/system/stats/sync-consumer-count/#%s#", queueName);

						int size = 0;
						AtomicInteger count = synConsumersCount.get(queueName);
						if (count != null)
						{
							size = count.get();
						}

						String content = GcsInfo.getAgentName() + "#" + queueName + "#" + size;

						InternalPublisher.send(ctName, content);
					}
				}
				catch (Throwable t)
				{
					log.error(t.getMessage(), t);
				}

			}
		};
		BrokerExecutor.scheduleWithFixedDelay(counter, 20, 20, TimeUnit.SECONDS);
	}

	public static void removeSession(ChannelHandlerContext ctx)
	{
		// Set<String> attributeKeys = channel.getAttributeKeys();
		// Channel channel = ctx.getChannel();
		Set<String> attributeKeys = ChannelAttributes.getAttributeKeys(ctx);
		for (String attributeKey : attributeKeys)
		{
			if (attributeKey.toString().startsWith(SESSION_ATT_PREFIX))
			{
				Object attributeValue = ChannelAttributes.get(ctx, attributeKey);
				if (attributeValue instanceof SynchronousMessageListener)
				{
					SynchronousMessageListener listener = (SynchronousMessageListener) attributeValue;
					pollStoped(listener.getsubscriptionKey());
					QueueProcessorList.removeListener(listener);
				}
			}
		}
	}

	public static void poll(NetPoll poll, ChannelHandlerContext ctx)
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
			Object attribute = ChannelAttributes.get(ctx, composedQueueName);

			SynchronousMessageListener msgListener = null;
			if (attribute != null)
			{
				msgListener = (SynchronousMessageListener) attribute;
			}
			else
			{
				ListenerChannel lchannel = new ListenerChannel(channel);

				msgListener = new SynchronousMessageListener(lchannel, queueName);

				ChannelAttributes.set(ctx, composedQueueName, msgListener);
				QueueProcessorList.get(queueName).add(msgListener);
				AtomicInteger previous = synConsumersCount.putIfAbsent(poll.getDestination(), new AtomicInteger(1));
				if (previous != null)
				{
					previous.incrementAndGet();
				}
			}
			msgListener.activate(poll.getTimeout(), poll.getActionId());
		}
		catch (Throwable t)
		{
			try
			{
				((BrokerProtocolHandler) ctx.getHandler()).exceptionCaught(channel, t, null);
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
}