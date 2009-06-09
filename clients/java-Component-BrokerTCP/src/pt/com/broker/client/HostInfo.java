package pt.com.broker.client;

/**
 * 
 * Immutable class that represents an Agent host.
 *
 */

public final class HostInfo
{
	private String hostname;
	private int port;

	/**
	 * Creates a HostInfo instance.
	 * @param hostname The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
	 * @param port Connection port.
	 */
	public HostInfo(String hostname, int port)
	{
		this.hostname = hostname;
		this.port = port;
	}

	public String getHostname()
	{
		return hostname;
	}

	public int getPort()
	{
		return port;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!obj.getClass().equals(this.getClass()))
			return false;
		HostInfo other = (HostInfo) obj;
		if (!hostname.equals(other.hostname))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return String.format("HostInfo - Hostname: %s, Port: %s", hostname, port);
	}
}
