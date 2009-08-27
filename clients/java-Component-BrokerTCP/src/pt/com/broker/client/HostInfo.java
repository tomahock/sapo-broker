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
	private final int udpPort;

	/**
	 * Creates a HostInfo instance.
	 * 
	 * @param hostname
	 *            The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
	 * @param port
	 *            Connection port.
	 */
	public HostInfo(String hostname, int port)
	{
		this(hostname, port, -1);
	}

	/**
	 * Creates a HostInfo instance.
	 * 
	 * @param hostname
	 *            The name of the host (e.g. broker.localdomain.company.com or 10.12.10.120).
	 * @param port
	 *            Connection port.
	 * @param udpPort
	 *            UDP port
	 */
	public HostInfo(String hostname, int port, int udpPort)
	{
		this.hostname = hostname;
		this.port = port;
		this.udpPort = udpPort;
	}

	public String getHostname()
	{
		return hostname;
	}

	public int getPort()
	{
		return port;
	}

	public int getUdpPort()
	{
		return udpPort;
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
		if (udpPort != other.udpPort)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return String.format("HostInfo - Hostname: %s, Port: %s, UDP Port: %s", hostname, port, udpPort);
	}
}
