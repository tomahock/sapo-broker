package pt.com.broker.messaging;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.messaging.TopicSubscriberList.MaximumDistinctSubscriptionsReachedException;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetUnsubscribe;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.gcs.messaging.DispatcherList;

/**
 * BrokerConsumer is responsible for managing client subscriptions.
 * 
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
	}

	public void listen(NetSubscribe subscription, Channel channel, boolean ackRequired)
	{
		try
		{
			QueueSessionListener qsl = QueueSessionListenerList.get(subscription.getDestination());
			if(qsl == null)
			{
				log.error("Failed to obtain a QueueSessionListener");
				return;
			}
			qsl.addConsumer(channel, ackRequired);
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
			TopicSubscriber subscriber = TopicSubscriberList.get(sb.getDestination());
			subscriber.addConsumer(channel, false);
		}

		catch (Throwable e)
		{
			if (e.getCause() instanceof MaximumDistinctSubscriptionsReachedException)
			{
				return false;
			}

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
		if (dtype == DestinationType.TOPIC)
		{
			try
			{
				TopicSubscriber subscriber = TopicSubscriberList.get(dname);
				if(subscriber == null)
				{
					log.error("Failed to obtain a TopicSubscriber");
					return;
				}
				subscriber.removeSessionConsumer(channel);
			}
			catch (MaximumDistinctSubscriptionsReachedException e)
			{
				// Doesn't happen
			}
		}
		else if (dtype == DestinationType.QUEUE || dtype == DestinationType.VIRTUAL_QUEUE)
		{
			QueueSessionListener qsl = QueueSessionListenerList.get(dname);
			qsl.removeSessionConsumer(channel);
		}
	}
}