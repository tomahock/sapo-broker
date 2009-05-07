package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import javax.crypto.BadPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkConnector
{
	private static final Logger log = LoggerFactory.getLogger(NetworkConnector.class);

	private Socket _client;
	private DataInputStream _rawi = null;
	private DataOutputStream _rawo = null;
	private SocketAddress _addr;
	private String _saddr;
	private volatile boolean closed = true;

	private final BrokerProtocolHandler protocolHandler;

	private HostInfo hostInfo = null;

	private long connectionVersion;

	public NetworkConnector(BrokerProtocolHandler protocolHandler, HostInfo hostInfo)
	{
		this.protocolHandler = protocolHandler;
		this.hostInfo = hostInfo;
	}

	public synchronized void connect() throws Throwable
	{
		if(hostInfo == null)
		{
			throw new Exception("NetworkConnector: Unable to connect - no host information available");
		}
		connect(this.hostInfo, 0);
	}

	public synchronized void connect(HostInfo host, long connectionVersion) throws Throwable
	{
		log.warn("Trying to connect");
		this.setConnectionVersion(connectionVersion);
		this.hostInfo = host;
		_client = new Socket(host.getHostname(), host.getPort());
		_rawo = new DataOutputStream(getSocket().getOutputStream());
		_rawi = new DataInputStream(getSocket().getInputStream());
		_addr = getSocket().getRemoteSocketAddress();
		_saddr = _addr.toString();
		log.info("Connection established: " + _saddr);
		closed = false;
	}

	public synchronized DataInputStream getInput()
	{
		return _rawi;
	}

	public synchronized DataOutputStream getOutput()
	{
		return _rawo;
	}

	public synchronized void close()
	{
		if(isClosed())
			return;
		closed = true;
		try
		{
			_rawi.close();
		}
		catch (Throwable e)
		{
		}

		try
		{
			_rawo.close();
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

	public synchronized  boolean isInputShutdown()
	{
		return getSocket().isInputShutdown();
	}

	public synchronized boolean isOutputShutdown()
	{
		return getSocket().isOutputShutdown();
	}

	public synchronized SocketAddress getInetAddress()
	{
		return _addr;
	}

	public synchronized  String getAddress()
	{
		return _saddr;
	}

	public synchronized Socket getSocket()
	{
		return _client;
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
}
