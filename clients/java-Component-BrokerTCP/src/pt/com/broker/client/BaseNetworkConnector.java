package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseNetworkConnector provides the basic functionality to TCP networking.
 * 
 */

public abstract class BaseNetworkConnector
{

	private static final Logger log = LoggerFactory.getLogger(BaseNetworkConnector.class);

	protected Socket client;
	protected DataInputStream rawInput = null;
	protected DataOutputStream rawOutput = null;
	protected SocketAddress socketAddress;
	protected String socketAddressLiteral;
	protected volatile boolean closed = true;
	private BrokerProtocolHandler protocolHandler;
	protected HostInfo hostInfo = null;
	private long connectionVersion;

	public BaseNetworkConnector(HostInfo hostInfo)
	{
		this.hostInfo = hostInfo;
	}

	public synchronized DataInputStream getInput()
	{
		return rawInput;
	}

	public synchronized DataOutputStream getOutput()
	{
		return rawOutput;
	}

	public synchronized void close()
	{
		if (isClosed())
			return;
		closed = true;
		try
		{
			rawInput.close();
		}
		catch (Throwable e)
		{
		}

		try
		{
			rawOutput.close();
		}
		catch (Throwable e)
		{
		}

		try
		{
			getSocket().close();
		}
		catch (Throwable e)
		{
		}
	}

	public synchronized boolean isConnected()
	{
		return getSocket().isConnected();
	}

	public synchronized boolean isInputShutdown()
	{
		return getSocket().isInputShutdown();
	}

	public synchronized boolean isOutputShutdown()
	{
		return getSocket().isOutputShutdown();
	}

	public synchronized SocketAddress getInetAddress()
	{
		return socketAddress;
	}

	public synchronized String getAddress()
	{
		return socketAddressLiteral;
	}

	public synchronized Socket getSocket()
	{
		return client;
	}

	public synchronized HostInfo getHostInfo()
	{
		return hostInfo;
	}

	public synchronized boolean isClosed()
	{
		return closed;
	}

	public synchronized void setConnectionVersion(long connectionVersion)
	{
		this.connectionVersion = connectionVersion;
	}

	public synchronized long getConnectionVersion()
	{
		return connectionVersion;
	}

	public synchronized void connect() throws Throwable
	{
		if (hostInfo == null)
		{
			throw new Exception("NetworkConnector: Unable to connect - no host information available");
		}
		connect(this.hostInfo, 0);
	}

	protected abstract void connect(HostInfo host, long connectionVersion) throws Throwable;

	public void setProtocolHandler(BrokerProtocolHandler protocolHandler)
	{
		this.protocolHandler = protocolHandler;
	}

	public BrokerProtocolHandler getProtocolHandler()
	{
		return protocolHandler;
	}
}
