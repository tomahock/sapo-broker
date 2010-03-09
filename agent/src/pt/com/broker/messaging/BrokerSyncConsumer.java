package pt.com.broker.messaging;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.broker.net.BrokerProtocolHandler;
import pt.com.broker.types.ChannelAttributes;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetPoll;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.LocalQueueConsumers;
import pt.com.gcs.messaging.MessageType;
import pt.com.gcs.messaging.QueueProcessorList;

/**
 * BrokerSyncConsumer represents a queue synchronous consumer.
 * 
 */
public class BrokerSyncConsumer
{
	private static final Logger log = LoggerFactory.getLogger(BrokerSyncConsumer.class);

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
						if(count != null)
						{
							size = count.get();
						}
												
						String content = GcsInfo.getAgentName() + "#" + queueName + "#" + size;

						NetBrokerMessage brkMessage = new NetBrokerMessage(content.getBytes("UTF-8"));

						InternalMessage intMsg = new InternalMessage();
						intMsg.setContent(brkMessage);
						intMsg.setDestination(ctName);
						intMsg.setType(MessageType.COM_TOPIC);

						Gcs.publish(intMsg);
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
	
	
	public static void poll(NetPoll poll, ChannelHandlerContext ctx)
	{
		Channel channel = ctx.getChannel();
		try
		{
			QueueProcessorList.get(poll.getDestination());
			
			String destination = poll.getDestination();
			String composedQueueName = SynchronousMessageListener.getComposedQueueName(destination);
			Object attribute = ChannelAttributes.get(ctx, composedQueueName);
			
			SynchronousMessageListener msgListener = null;
			if (attribute != null)
			{
				msgListener = (SynchronousMessageListener) attribute;
			}
			else
			{
				msgListener = new SynchronousMessageListener(destination, channel);
				ChannelAttributes.set(ctx, composedQueueName, msgListener);
				LocalQueueConsumers.add(destination, msgListener);
				AtomicInteger previous = synConsumersCount.putIfAbsent(poll.getDestination(), new AtomicInteger(1));
				if(previous != null)
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
	
	static final AtomicInteger zeroValue = new AtomicInteger(0);
	public static void pollStoped(String destination)
	{
		AtomicInteger count = synConsumersCount.get(destination);
		if(count != null)
		{
			count.decrementAndGet();
			synConsumersCount.remove(destination, zeroValue); // remove if zero
		}
	}

}
