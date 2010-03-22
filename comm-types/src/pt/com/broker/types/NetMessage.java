package pt.com.broker.types;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Messaging level message.
 * 
 */

public class NetMessage implements DeliverableMessage
{

	private NetAction action;
	private Map<String, String> headers;

	public NetMessage(NetAction action)
	{
		this(action, null);
	}

	public NetMessage(NetAction action, Map<String, String> headers)
	{
		try
		{
			this.action = action;
			if (headers != null)
			{
				this.headers = headers;
			}
			else
				this.headers = new HashMap<String, String>();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	public NetAction getAction()
	{
		return action;
	}
}
