package pt.com.broker.monitorization.collectors;

import java.util.ArrayList;
import java.util.List;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

/***
 * Collector is a base class for all elements responsible for collecting agents information.
 * 
 */

public abstract class Collector<T>
{
	final private String collectorName;
	final private BaseBrokerClient brokerClient;
	final private String subscription;

	protected List<T> listeners = new ArrayList<T>();

	/**
	 * 
	 * @param name
	 *            Collector name/function.
	 * @param agent
	 *            Agent information.
	 * @throws Throwable
	 *             Thrown when BrokerClient initialization throws a Throwable.
	 */
	public Collector(String name, String subscription, BaseBrokerClient agent) throws Throwable
	{
		collectorName = name;
		this.subscription = subscription;
		brokerClient = agent;
	}

	/***
	 * Start collection.
	 */
	public void start() throws Throwable
	{
		BaseBrokerClient bc = getBrokerClient();

		NetSubscribe netSub = new NetSubscribe(subscription, DestinationType.TOPIC);
		bc.addAsyncConsumer(netSub, new BrokerListener()
		{

			@Override
			public boolean isAutoAck()
			{
				return false;
			}

			@Override
			public void onMessage(NetNotification message)
			{
				messageReceived(message);
			}

		});
	}

	/***
	 * Stop collection.
	 * 
	 * @throws Throwable
	 */
	public void stop() throws Throwable
	{
		brokerClient.unsubscribe(DestinationType.TOPIC, subscription);
		brokerClient.close();
	}

	/**
	 * @return Returns the collector name. It should reflect its function.
	 */
	public String getCollectorName()
	{
		return collectorName;
	}

	/**
	 * Handle message received.
	 * 
	 * @param notification
	 *            NetNotification object received
	 */
	protected abstract void messageReceived(NetNotification notification);

	/**
	 * @return Returns a BrokerClient instance.
	 */
	protected BaseBrokerClient getBrokerClient()
	{
		return brokerClient;
	}

	/**
	 * Add a listener that will be called when a new collection is performed.
	 * 
	 * @param listener
	 *            An object implementing T
	 */
	public void addListener(T listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}

}
