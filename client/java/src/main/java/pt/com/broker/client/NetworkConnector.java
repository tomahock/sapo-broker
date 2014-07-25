package pt.com.broker.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * NetworkConnector is an implementation of BaseNetworkConnector for plain TCP connections.
 */
public class NetworkConnector extends BaseNetworkConnector
{
	private static final Logger log = LoggerFactory.getLogger(NetworkConnector.class);

	public NetworkConnector(HostInfo hostInfo)
	{
		super(hostInfo);
	}

	public synchronized void connect(HostInfo host, long connectionVersion) throws Throwable
	{
		try
		{
			log.debug("Trying to connect");
			this.setConnectionVersion(connectionVersion);
			this.hostInfo = host;
			client = new Socket();

			client.setReceiveBufferSize(256 * 1024);
			client.setSendBufferSize(256 * 1024);
			client.setSoLinger(true, 2);
			client.setSoTimeout(host.getReadTimeout());

			client.connect(new InetSocketAddress(host.getHostname(), host.getPort()), host.getConnectTimeout());
			rawOutput = new DataOutputStream(getSocket().getOutputStream());
			rawInput = new DataInputStream(getSocket().getInputStream());
			socketAddress = getSocket().getRemoteSocketAddress();
			socketAddressLiteral = socketAddress.toString();
			log.debug("Connection established: " + socketAddressLiteral);
			closed = false;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
}
