package pt.com.broker.client;

public final class HostInfo
{
	private String hostname;
	private int port;
	private int sslPort;

	public HostInfo(String hostname, int port)
	{
		this(hostname, port, 0);
	}

	public HostInfo(String hostname, int port, int sslPort)
	{
		this.hostname = hostname;
		this.port = port;
		this.sslPort = sslPort;
	}

	public String getHostname()
	{
		return hostname;
	}

	public int getPort()
	{
		return port;
	}

	public int getSslPort()
	{
		return sslPort;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!obj.getClass().equals(this.getClass()))
			return false;
		HostInfo other = (HostInfo)obj;
		if(!hostname.equals(other.hostname))
			return false;
		if(port != other.port)
			return false;
		if(sslPort != other.sslPort)
			return false;
		return true;
	}
	
	@Override
	public String toString()
	{
		return String.format("HostInfo - Hostname: %s, Port: %s, SslPort: %s", hostname, port, sslPort);
	}
}
