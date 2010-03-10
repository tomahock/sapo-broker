package pt.com.broker.messaging;

import org.jboss.netty.channel.Channel;

import pt.com.broker.messaging.TopicSubscriberList.MaximumDistinctSubscriptionsReachedException;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetUnsubscribe;
import pt.com.broker.types.NetAction.DestinationType;

/**
 * BrokerConsumer is responsible for managing client subscriptions.
 * 
 */

public class BrokerConsumer
{
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