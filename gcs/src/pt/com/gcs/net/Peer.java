package pt.com.gcs.net;

import org.caudexorigo.text.StringUtils;

/**
 * Peer holds information about an agent.
 * 
 */

public class Peer
{
	private final String _name;

	private final String _host;

	private final int _port;

	private final String _address;

	/**
	 * Creates a Peer instance.
	 * 
	 * @param name
	 *            Agent's name
	 * @param host
	 *            Agent's address or network name.
	 * @param port
	 *            Agent's port.
	 */

	public Peer(String name, String host, int port)
	{
		this(name, host, port, host + ":" + port);
	}

	public Peer(String name, String host, int port, String address)
	{
		_name = name;
		_host = host;
		_port = port;
		this._address = address;
	}

	public String getName()
	{
		return _name;
	}

	public String getHost()
	{
		return _host;
	}

	public int getPort()
	{
		return _port;
	}

	public String getAddress()
	{
		return _address;
	}

	public static Peer createPeerFromHelloMessage(String helloMessage)
	{
		String peerName = StringUtils.substringBefore(helloMessage, "@");
		String peerAddr = StringUtils.substringAfter(helloMessage, "@");
		String peerHost = StringUtils.substringBefore(peerAddr, ":");
		try
		{
			int peerPort = Integer.parseInt(StringUtils.substringAfter(peerAddr, ":"));
			return new Peer(peerName, peerHost, peerPort, peerAddr);
		}
		catch (NumberFormatException nfe)
		{

		}
		return null;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_host == null) ? 0 : _host.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + _port;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Peer other = (Peer) obj;
		if (_host == null)
		{
			if (other._host != null)
				return false;
		}
		else if (!_host.equals(other._host))
			return false;
		if (_name == null)
		{
			if (other._name != null)
				return false;
		}
		else if (!_name.equals(other._name))
			return false;
		if (_port != other._port)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return _name + "#" + _host + ":" + _port;
	}
}
