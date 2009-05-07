package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslNetworkConnector
{
	private static final Logger log = LoggerFactory.getLogger(SslNetworkConnector.class);

	private static final int MAX_NUMBER_OF_TRIES = 5;

	private Socket _client;
	private DataInputStream _rawi = null;
	private DataOutputStream _rawo = null;
	private SocketAddress _addr;
	private volatile boolean closed = true;

	private String _saddr;

	private final BrokerProtocolHandler protocolHandler;

	private HostInfo hostInfo = null;

	private final String keystoreLocation;

	private final char[] keystorePw;

	private long connectionVersion;

	public SslNetworkConnector(BrokerProtocolHandler protocolHandler, HostInfo hostInfo, String keystoreLocation, char[] keystorePw) throws UnknownHostException, IOException
	{
		this.protocolHandler = protocolHandler;
		this.hostInfo = hostInfo;
		this.keystoreLocation = keystoreLocation;
		this.keystorePw = keystorePw;

	}

	private SocketFactory getSslSocketFactory(String keystoreLocation, char[] keystorePw)
	{
		SocketFactory sf = null;
		try
		{
			KeyStore keyStore = KeyStore.getInstance("JKS");

			keyStore.load(new FileInputStream(keystoreLocation), keystorePw);

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(keyStore);

			javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("SSLv3");
			sslContext.init(null, tmf.getTrustManagers(), null);

			sf = sslContext.getSocketFactory();
		}
		catch (Throwable t)
		{
			log.error("SslNetworkConnector.SslNetworkConnector", t);
			throw new RuntimeException(t);
		}

		return sf;
	}

	public synchronized void connect() throws Throwable
	{
		if (hostInfo == null)
		{
			throw new Exception("NetworkConnector: Unable to connect - no host information available");
		}
		connect(this.hostInfo, 0);
	}

	public synchronized void connect(HostInfo host, long connectionVersion) throws Throwable
	{
		log.warn("Trying to reconnect (SSL)");
		this.hostInfo = host;
		this.setConnectionVersion(connectionVersion);
		SocketFactory socketFactory = null;

		if (StringUtils.isBlank(keystoreLocation))
			socketFactory = SSLSocketFactory.getDefault();
		else
			socketFactory = getSslSocketFactory(keystoreLocation, keystorePw);

		_client = new Socket(host.getHostname(), host.getPort());
		_client = socketFactory.createSocket(hostInfo.getHostname(), hostInfo.getSslPort());
		getSocket().setSoTimeout(0);

		_rawo = new DataOutputStream(getSocket().getOutputStream());
		_rawi = new DataInputStream(getSocket().getInputStream());
		_addr = getSocket().getRemoteSocketAddress();
		_saddr = _addr.toString();
		log.info("Connection established (SSL): " + _saddr);
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
		return _addr;
	}

	public synchronized String getAddress()
	{
		return _saddr;
	}

	public synchronized Socket getSocket()
	{
		return _client;
	}

	public synchronized void setHostInfo(HostInfo hostInfo)
	{
		this.hostInfo = hostInfo;
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
