package pt.com.broker.types;

import java.util.Map;

/**
 * Represents a Notification message.
 * 
 */

public final class NetNotification
{
	private String destination;
	private String subscription;
	private NetAction.DestinationType destinationType;
	private NetBrokerMessage message;

	private Map<String, String> headers;

	public NetNotification(String destination, NetAction.DestinationType destinationType, NetBrokerMessage message, String subscription)
	{
		this.destination = destination;
		this.destinationType = destinationType;
		this.message = message;
		if (subscription == null)
			this.subscription = "";
		else
			this.subscription = subscription;
	}

	public String getDestination()
	{
		return destination;
	}

	public String getSubscription()
	{
		return subscription;
	}

	public NetAction.DestinationType getDestinationType()
	{
		return destinationType;
	}

	public NetBrokerMessage getMessage()
	{
		return message;
	}

	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}
}