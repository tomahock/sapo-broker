package pt.com.broker.performance;

import java.util.concurrent.Callable;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;

public abstract class TestActor implements Callable<Integer>, BrokerListener
{
	private final BrokerClient brokerClient;

	protected TestActor(BrokerClient brokerClient)
	{
		this.brokerClient = brokerClient;
	}

	public void close()
	{
		getBrokerClient().close();
	}

	public BrokerClient getBrokerClient()
	{
		return brokerClient;
	}
}
