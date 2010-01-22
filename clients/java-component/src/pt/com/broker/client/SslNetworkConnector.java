package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NetworkConnector is an implementation of BaseNetworkConnector for secure TCP connections (SSL).
 * 
 */

public class SslNetworkConnector extends BaseNetworkConnector
{
	private static final Logger log = LoggerFactory.getLogger(SslNetworkConnector.class);

	private final String keystoreLocation;

	private final char[] keystorePw;

	public SslNetworkConnector(HostInfo hostInfo) throws UnknownHostException, IOException
	{
		this(hostInfo, null, null);
	}

	public SslNetworkConnector(HostInfo hostInfo, String keystoreLocation, char[] keystorePw) throws UnknownHostException, IOException
	{
		super(hostInfo);
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

		// client = socketFactory.createSocket(hostInfo.getHostname(), hostInfo.getPort());
		client = socketFactory.createSocket();
		client.connect(new InetSocketAddress(host.getHostname(), host.getPort()), 15 * 1000);
		getSocket().setSoTimeout(0);

		rawOutput = new DataOutputStream(getSocket().getOutputStream());
		rawInput = new DataInputStream(getSocket().getInputStream());
		socketAddress = getSocket().getRemoteSocketAddress();
		socketAddressLiteral = socketAddress.toString();
		log.info("Connection established (SSL): " + socketAddressLiteral);
		closed = false;
	}
}
