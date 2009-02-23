package pt.com.broker.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.com.broker.client.messaging.BrokerListener;
import pt.com.types.NetNotification;
import pt.com.types.NetSubscribe;

public class BrokerAsyncConsumer
{
	private final NetSubscribe subscription;

	private final BrokerListener _wrappedListener;

	private final Pattern _subscriptionName;

	public BrokerAsyncConsumer(NetSubscribe subscrition, BrokerListener listener)
	{
		super();
		_wrappedListener = listener;
		this.subscription = subscrition;
		_subscriptionName = Pattern.compile(subscrition.getDestination());
	}

	public NetSubscribe getSubscription()
	{
		return subscription;
	}

	public BrokerListener getListener()
	{
		return _wrappedListener;
	}

	public boolean deliver(NetNotification msg)
	{

		Matcher m = _subscriptionName.matcher(msg.getDestination());
		if (m.matches())
		{
			_wrappedListener.onMessage(msg);
			return true;
		}
		else
		{
			return false;
		}
	}

}
