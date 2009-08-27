package pt.com.broker.functests.positive;

import pt.com.broker.functests.helpers.GenericPubSubTest;
import pt.com.broker.types.NetAction.DestinationType;

public class QueueTest extends GenericPubSubTest
{

	public QueueTest()
	{
		this("Queue with single recipient");
	}

	public QueueTest(String testName)
	{
		super(testName);
		setDestinationType(DestinationType.QUEUE);
	}

}
