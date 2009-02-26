package pt.com.types;

import java.util.HashMap;
import java.util.Map;

public class NetMessage
{

	private NetAction action;
	private Map<String, String> headers;

	public NetMessage(NetAction action)
	{
		this(action, null);
	}

	public NetMessage(NetAction action, Map<String, String> headers)
	{
		this.action = action;
		if (headers != null)
			this.headers = headers;
		else
			this.headers = new HashMap<String, String>();
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	public NetAction getAction()
	{
		return action;
	}

	/*
	 * TODO : add this information: Encoding type Encoding version major Encoding version major
	 */
}
