package pt.com.broker.functests.helpers;

import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;

public class GenericBrokerListener implements BrokerListener
{

	private NetAction.DestinationType destinationType;
	private SetValueFuture<NetNotification> value = new SetValueFuture<NetNotification>();

	public GenericBrokerListener(NetAction.DestinationType destinationType)
	{
		this.destinationType = destinationType;
	}

	@Override
	public boolean isAutoAck()
	{
		return destinationType != DestinationType.TOPIC;
	}

	@Override
	public void onMessage(NetNotification message)
	{
		value.set(message);
	}

	public SetValueFuture<NetNotification> getFuture()
	{
		return value;
	}
}
