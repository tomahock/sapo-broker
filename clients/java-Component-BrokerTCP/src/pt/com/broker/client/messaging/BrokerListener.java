package pt.com.broker.client.messaging;

import pt.com.types.NetNotification;

public interface BrokerListener
{
	public void onMessage(NetNotification message);

	public boolean isAutoAck();
}
