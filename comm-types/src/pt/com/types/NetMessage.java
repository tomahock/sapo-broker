package pt.com.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetMessage
{

	private NetAction action;
	private List<NetParameter> headers;

	public NetMessage(NetAction action)
	{
		this(action, null);
	}

	public NetMessage(NetAction action, List<NetParameter> headers)
	{
		this.action = action;
		if (headers != null)
			this.headers = headers;
		else
			this.headers = new ArrayList<NetParameter>(0);
	}

	public Iterator<NetParameter> getHeaders()
	{
		return headers.iterator();
	}

	public NetAction getAction()
	{
		return action;
	}

	/*
	 * TODO : add this information: Encoding type Encoding version major Encoding version major
	 */
}
