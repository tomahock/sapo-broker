package pt.com.broker.monitorization.collectors;

import java.util.regex.Pattern;

import pt.com.broker.client.HostInfo;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetAction.DestinationType;


/**
 * SubscriptionCountCollector collects information about the number of subscriptions.
 *
 */
public class SubscriptionCountCollector extends Collector<SubscriptionCountListener>
{
	//	Destination sample: /system/stats/queue-consumer-count/#/queue/foo#
	//  Payload sample: broker1#/queue/foo#2
	
	private static final String SUBSCRIPTION = "/system/stats/.*-consumer-count/.*"; //TODO: optimize this
	
	int subsIndexDif = 14; // index of 't' or 'q' /system/stats/topic-consumer-count or /system/stats/queue-consumer-count ...
	
	private static final String agentSubsCountRegEx = "#";
	Pattern agentSubsCountPattern;
	
	public SubscriptionCountCollector(HostInfo hostInfo) throws Throwable
	{
		super("Subscription Count Collector", hostInfo, SUBSCRIPTION);
		agentSubsCountPattern = Pattern.compile(agentSubsCountRegEx);
	}
	
	@Override
	protected void messageReceived(NetNotification notification)
	{
		String agent = null;
		DestinationType subscriptionType = null;
		String subscriptionName = null;
		int count = 0;
		
		subscriptionType = notification.getDestination().charAt(subsIndexDif) == 't' ? DestinationType.TOPIC : DestinationType.QUEUE;
		
		String payload = new String(notification.getMessage().getPayload());
		String[] tokens = agentSubsCountPattern.split(payload);
		
		agent = tokens[0];
		subscriptionName = tokens[1];
		count = Integer.parseInt(tokens[2]);
		
		// For now, show 'this' subscription
//		if(subscriptionName.equals(SUBSCRIPTION))
//			return;
		
		synchronized (listeners)
		{
			for (SubscriptionCountListener handler : listeners)
			{
				try{
					handler.onUpdate(agent, subscriptionType, subscriptionName, count);
				}catch (Exception e) {
					// TODO: log exception
				}
			}
		}
	}
	
	
	
}
