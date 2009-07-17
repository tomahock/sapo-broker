package pt.com.broker.monitorization.collectors;

import pt.com.broker.types.NetAction.DestinationType;

public interface SubscriptionCountListener
{
	void onUpdate(String agentName, DestinationType subscriptionType, String subscriptionName, int count);
}
