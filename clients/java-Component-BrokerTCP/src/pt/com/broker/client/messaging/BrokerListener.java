package pt.com.broker.client.messaging;

import pt.com.types.NetNotification;

public interface BrokerListener
{
	void onMessage(NetNotification message);
	boolean isAutoAck();
}