package pt.com.broker.monitorization.collectors;


public interface SubscriptionCountListener
{
	void onUpdate(String agentName, String subscriptionType, String subscriptionName, int count);
}
