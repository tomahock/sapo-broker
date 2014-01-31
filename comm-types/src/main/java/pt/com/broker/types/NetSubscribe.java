package pt.com.broker.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Subscription message.
 * 
 */

public final class NetSubscribe
{
	private String actionId;
	private String destination;
	private NetAction.DestinationType destinationType;

	private Map<String, String> headers;

	public NetSubscribe(String destination, NetAction.DestinationType destinationType)
	{
		this.destination = destination;
		this.destinationType = destinationType;
	}

	public void setActionId(String actionId)
	{
		this.actionId = actionId;
	}

	public String getActionId()
	{
		return actionId;
	}

	public String getDestination()
	{
		return destination;
	}

	public NetAction.DestinationType getDestinationType()
	{
		return destinationType;
	}

	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	public void addHeader(String header, String value)
	{
		if (headers == null)
		{
			headers = new HashMap<String, String>();
		}
		headers.put(header, value);
	}
}
