package pt.com.broker.monitorization.collectors;

import java.util.regex.Pattern;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.types.NetNotification;

/**
 * SubscriptionCountCollector collects information about the number of subscriptions.
 * 
 */
public class SubscriptionCountCollector extends Collector<SubscriptionCountListener>
{
	// Destination sample: /system/stats/queue-consumer-count/#/queue/foo#
	// Payload sample: broker1#/queue/foo#2

	private static final String SUBSCRIPTION = "/system/stats/.*-consumer-count/.*"; // TODO: optimize this

	int subsIndexDif = 14; // index of 't', 'q' or 's' /system/stats/topic-consumer-count or /system/stats/queue-consumer-count ...

	private static final String agentSubsCountRegEx = "#";
	Pattern agentSubsCountPattern;

	public SubscriptionCountCollector(BaseBrokerClient agent) throws Throwable
	{
		super("Subscription Count Collector", SUBSCRIPTION, agent);
		agentSubsCountPattern = Pattern.compile(agentSubsCountRegEx);
	}

	@Override
	protected void messageReceived(NetNotification notification)
	{
		String agent = null;
		String subscriptionName = null;
		int count = 0;

		String subscriptionType = null;
		switch (Character.toLowerCase(notification.getDestination().charAt(subsIndexDif)))
		{
		case 't':
			subscriptionType = "TOPIC";
			break;
		case 'q':
			subscriptionType = "QUEUE";
			break;
		case 's':
			subscriptionType = "SYNC";
			break;
		default:
			return;

		}

		String payload = new String(notification.getMessage().getPayload());
		String[] tokens = agentSubsCountPattern.split(payload);

		agent = tokens[0];
		subscriptionName = tokens[1];
		count = Integer.parseInt(tokens[2]);

		// For now, show 'this' subscription
		// if(subscriptionName.equals(SUBSCRIPTION))
		// return;

		synchronized (listeners)
		{
			for (SubscriptionCountListener handler : listeners)
			{
				try
				{
					handler.onUpdate(agent, subscriptionType, subscriptionName, count);
				}
				catch (Exception e)
				{
					// TODO: log exception
				}
			}
		}
	}

}
