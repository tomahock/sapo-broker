package pt.com.broker.monitorization.collectors;

public interface QueueSizeListener
{
	void onUpdate(String agentName, String queueName, int size);
}
