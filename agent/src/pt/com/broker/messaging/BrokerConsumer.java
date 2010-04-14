package pt.com.broker.messaging;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.text.DateUtil;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.core.BrokerExecutor;
import pt.com.broker.types.ListenerChannel;
import pt.com.broker.types.MessageListener;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetUnsubscribe;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.conf.GcsInfo;
import pt.com.gcs.messaging.Gcs;
import pt.com.gcs.messaging.InternalPublisher;
import pt.com.gcs.messaging.QueueProcessor;
import pt.com.gcs.messaging.QueueProcessorList;
import pt.com.gcs.messaging.TopicProcessor;
import pt.com.gcs.messaging.TopicProcessorList;

/**
 * BrokerConsumer is responsible for managing client subscriptions.
 */

public class BrokerConsumer
{
	private static final Logger log = LoggerFactory.getLogger(BrokerConsumer.class);

	private static BrokerConsumer instance = new BrokerConsumer();

	public static BrokerConsumer getInstance()
	{
		return instance;
	}

	private BrokerConsumer()
	{
		Runnable topic_consumer_counter = new Runnable()
		{
			public void run()
			{
				try
				{
					// New format
					
					final String topicSubscriptionTopic = "/system/stats/topics/";
					
					StringBuilder sb = new StringBuilder();
					sb.append(String.format("<qinfo date='%s' agent-name='%s'>", DateUtil.formatISODate(new Date()), GcsInfo.getAgentName()));
					
					for (TopicProcessor tp : TopicProcessorList.values())
					{
						int ssize = countLocal(tp.listeners());

						if (ssize > 0)
						{
							sb.append(String.format("\n	<item subject='topic://%s' predicate='subscriptions' value='%s' />", tp.getSubscriptionName(), ssize));
						}
					}
					
					sb.append("\n</qinfo>");
					
					String result = sb.toString();
					
					log.info('\n' + result);
					
					InternalPublisher.send(String.format("%s#%s#", topicSubscriptionTopic, GcsInfo.getAgentName()), result);
					
				}
				catch (Throwable e)
				{
					log.error(e.getMessage(), e);
				}
			}

			private int countLocal(Collection<MessageListener> listeners)
			{
				int s = 0;
				for (MessageListener l : listeners)
				{
					if ((l.getType() == MessageListener.Type.LOCAL))
					{
						s++;
					}
				}
				return s;
			}
		};

		Runnable queue_consumer_counter = new Runnable()
		{
			public void run()
			{
				try
				{					
					// New format
					
					final String queueSubscriptionTopic = "/system/stats/queues/";
					
					StringBuilder sb = new StringBuilder();
					sb.append(String.format("<qinfo date='%s' agent-name='%s'>", DateUtil.formatISODate(new Date()), GcsInfo.getAgentName()));
					
					for (QueueProcessor qs : QueueProcessorList.values())
					{
						int ssize = qs.localListeners().size();

						sb.append(String.format("\n	<item subject='queue://%s' predicate='subscriptions' value='%s' />", qs.getQueueName(), ssize));
					}
					
					sb.append("\n</qinfo>");
					
					String result = sb.toString();
					log.info('\n' + result);
					
					InternalPublisher.send(String.format("%s#%s#", queueSubscriptionTopic, GcsInfo.getAgentName()), result);
				}
				catch (Throwable t)
				{
					log.error(t.getMessage(), t);
				}
			}
		};

		BrokerExecutor.scheduleWithFixedDelay(topic_consumer_counter, 20, 20, TimeUnit.SECONDS);

		BrokerExecutor.scheduleWithFixedDelay(queue_consumer_counter, 20, 20, TimeUnit.SECONDS);
	}

	public void listen(NetSubscribe sb, Channel channel, boolean ackRequired)
	{
		try
		{
			ListenerChannel lchannel = new ListenerChannel(channel);
			BrokerQueueListener subscriber = new BrokerQueueListener(lchannel, sb.getDestination(), ackRequired);

			Gcs.addAsyncConsumer(sb.getDestination(), subscriber);
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public synchronized boolean subscribe(NetSubscribe sb, Channel channel)
	{
		try
		{
			ListenerChannel lchannel = new ListenerChannel(channel);

			BrokerTopicListener subscriber = new BrokerTopicListener(lchannel, sb.getDestination());

			Gcs.addAsyncConsumer(sb.getDestination(), subscriber);
		}

		catch (Throwable e)
		{
			if (e instanceof InterruptedException)
			{
				Thread.currentThread().interrupt();
			}
			throw new RuntimeException(e);
		}
		return true;
	}

	public synchronized void unsubscribe(NetUnsubscribe unsubs, Channel channel)
	{
		String dname = unsubs.getDestination();
		DestinationType dtype = unsubs.getDestinationType();

		ListenerChannel lchannel = new ListenerChannel(channel);
		if (dtype == DestinationType.TOPIC)
		{
			BrokerTopicListener subscriber = new BrokerTopicListener(lchannel, dname);
			TopicProcessorList.removeListener(subscriber);
		}
		else if (dtype == DestinationType.QUEUE || dtype == DestinationType.VIRTUAL_QUEUE)
		{
			QueueProcessorList.removeListener(new BrokerQueueListener(lchannel, dname, true));
		}
	}
}
