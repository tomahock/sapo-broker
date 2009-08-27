package pt.com.broker.client.messaging;

import pt.com.broker.types.NetNotification;

/**
 * BrokerListener interface should by asynchronous message consumers.
 * 
 */

public interface BrokerListener
{
	/**
	 * Fired when a message arrives.
	 * 
	 * @param message
	 *            The message.
	 */
	void onMessage(NetNotification message);

	/**
	 * Auto-acknowledge message
	 * 
	 * @return <code>true</code> if implementors wish that the infrastructure acknowledges the message, <code>false</code> otherwise.
	 */
	boolean isAutoAck();
}