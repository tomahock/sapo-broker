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

	private final String _host;
	private final int _port;

	private Socket _client;
	private DataInputStream _rawi = null;
	private DataOutputStream _rawo = null;
	private SocketAddress _addr;

	private String _saddr;
	public SslNetworkConnector(String host, int port) throws UnknownHostException, IOException
	{
		this(host, port, null, null);
	}
	
	public SslNetworkConnector(String host, int port, String keystoreLocation, char[] keystorePw) throws UnknownHostException, IOException
	{
		_host = host;
		_port = port;
		
		SocketFactory socketFactory = null;
		
		if(StringUtils.isBlank(keystoreLocation))
			socketFactory = SSLSocketFactory.getDefault();
		else 
			socketFactory = getSslSocketFactory(keystoreLocation, keystorePw);
			
		setSocket(socketFactory.createSocket(_host, _port));
		getSocket().setSoTimeout(0);
		_rawo = new DataOutputStream(getSocket().getOutputStream());
		_rawi = new DataInputStream(getSocket().getInputStream());

		_addr = getSocket().getRemoteSocketAddress();
		_saddr = _addr.toString();

		log.info("Receive Buffer Size: " + getSocket().getReceiveBufferSize());
		log.info("Send Buffer Size: " + getSocket().getSendBufferSize());
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

	public void reconnect(Throwable se)
	{
		log.warn("Connect Error: " + se.getMessage());

		close();

		Throwable ex = new Exception(se);

		while (ex != null)
		{
			try
			{
				log.error("Trying to reconnect");
				setSocket(new Socket(_host, _port));
				_rawo = new DataOutputStream(getSocket().getOutputStream());
				_rawi = new DataInputStream(getSocket().getInputStream());
				_addr = getSocket().getRemoteSocketAddress();
				_saddr = _addr.toString();

				ex = null;
				log.info("Connection established: " + _saddr);

			}
			catch (Exception re)
			{
				log.info("Reconnect failled");
				ex = re;
				Sleep.time(2000);
			}
		}
	}

	public DataInputStream getInput()
	{
		return _rawi;
	}

	public DataOutputStream getOutput()
	{
		return _rawo;
	}

	public void close()
	{
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

	public boolean isConnected()
	{
		return getSocket().isConnected();
	}

	public boolean isInputShutdown()
	{
		return getSocket().isInputShutdown();
	}

	public boolean isOutputShutdown()
	{
		return getSocket().isOutputShutdown();
	}

	public SocketAddress getInetAddress()
	{
		return _addr;
	}

	public String getAddress()
	{
		return _saddr;
	}

	public void setSocket(Socket _client)
	{
		this._client = _client;
	}

	public Socket getSocket()
	{
		return _client;
	}
}
