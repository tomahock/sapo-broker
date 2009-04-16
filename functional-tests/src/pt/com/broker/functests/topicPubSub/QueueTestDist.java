package pt.com.broker.functests.topicPubSub;

import pt.com.broker.client.BrokerClient;

public class QueueTestDist extends QueueTest
{
	public QueueTestDist()
	{
		this("Queue test with distant consumer");
	}

	public QueueTestDist(String testName)
	{
		super(testName);
		try
		{
			setInfoConsumer(new BrokerClient("127.0.0.1", 3423, "tcp://mycompany.com/mypublisher"));
		}
		catch (Throwable t)
		{
			setFailure(t);
		}

	}

}
