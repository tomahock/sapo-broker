package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetworkConnector is an implementation of BaseNetworkConnector for secure TCP connections (SSL).
 * 
 */

public class SslNetworkConnector extends BaseNetworkConnector
{
	private static final Logger log = LoggerFactory.getLogger(SslNetworkConnector.class);
	private SSLContext sslContext;
	private SSLSession ssl_session;

	public SslNetworkConnector(HostInfo hostInfo, javax.net.ssl.SSLContext sslContext) throws UnknownHostException, IOException
	{
		super(hostInfo);
		this.sslContext = sslContext;
	}

	private SocketFactory getSslSocketFactory()
	{
		SSLSocketFactory sf = null;
		try
		{
			sf = sslContext.getSocketFactory();

		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}

		return sf;
	}

	public synchronized void connect(HostInfo host, long connectionVersion) throws Throwable
	{
		log.warn("Trying to reconnect (SSL)");
		this.hostInfo = host;
		this.setConnectionVersion(connectionVersion);
		SocketFactory socketFactory = getSslSocketFactory();

		// client = socketFactory.createSocket(hostInfo.getHostname(), hostInfo.getPort());
		client = socketFactory.createSocket();
		client.setReceiveBufferSize(256 * 1024);
		client.setSendBufferSize(256 * 1024);
		client.connect(new InetSocketAddress(host.getHostname(), host.getPort()), 15 * 1000);
		getSocket().setSoTimeout(0);

		ssl_session = ((SSLSocket) client).getSession();

		rawOutput = new DataOutputStream(getSocket().getOutputStream());
		rawInput = new DataInputStream(getSocket().getInputStream());
		socketAddress = getSocket().getRemoteSocketAddress();
		socketAddressLiteral = socketAddress.toString();
		log.info("Connection established (SSL): " + socketAddressLiteral);
		closed = false;
	}

	protected SSLSession getSSLSession()
	{
		return ssl_session;
	}
}