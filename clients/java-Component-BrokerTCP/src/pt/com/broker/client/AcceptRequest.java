package pt.com.broker.client;

import pt.com.broker.client.messaging.MessageAcceptedListener;

/**
 * AcceptRequest is meat to be used by clients that specify an action identifier.
 * 
 */

public class AcceptRequest
{
	private String actionId;
	private MessageAcceptedListener listner;
	private long timeout;

	/**
	 * Creates an instance of AcceptRequest.
	 * 
	 * @param actionId
	 *            Message action identifier
	 * @param listner
	 *            An implementation of MessageAcceptedListener.
	 * @param timeout
	 *            A time interval, in milliseconds, during witch the Accept message is expected.
	 */
	public AcceptRequest(String actionId, MessageAcceptedListener listner, long timeout)
	{
		if (actionId == null)
			throw new IllegalArgumentException("actionId is null");
		if (listner == null)
			throw new IllegalArgumentException("listner is null");
		if (timeout <= 0)
			throw new IllegalArgumentException("timeout <= 0");

		this.actionId = actionId;
		this.listner = listner;
		this.timeout = timeout;
	}

	public String getActionId()
	{
		return actionId;
	}

	public MessageAcceptedListener getListner()
	{
		return listner;
	}

	public void setTimeout(long timeout)
	{
		this.timeout = timeout;
	}

	public long getTimeoutDelta()
	{
		return timeout;
	}

}
