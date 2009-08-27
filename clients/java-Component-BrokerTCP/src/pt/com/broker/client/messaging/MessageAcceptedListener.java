package pt.com.broker.client.messaging;

import pt.com.broker.types.NetFault;

/**
 * MessageAcceptedListener interface should be implemented by those who wish to be notified of an event related with messages that carry an action identifier.
 * 
 */

public interface MessageAcceptedListener
{
	void messageAccepted(String actionId);

	void messageTimedout(String actionId);

	/**
	 * An error occurred during message processing.
	 * 
	 * @param fault
	 *            Error description
	 */
	void messageFailed(NetFault fault);
}
