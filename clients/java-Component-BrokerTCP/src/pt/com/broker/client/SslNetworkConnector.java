package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.ssl.internal.ssl.SSLSocketImpl;

import javax.net.ssl.TrustManagerFactory;

import javax.net.ssl.SSLContext;

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
			
		_client = socketFactory.createSocket(_host, _port);
		_client.setSoTimeout(0);
		_rawo = new DataOutputStream(_client.getOutputStream());
		_rawi = new DataInputStream(_client.getInputStream());

		_addr = _client.getRemoteSocketAddress();
		_saddr = _addr.toString();

		log.info("Receive Buffer Size: " + _client.getReceiveBufferSize());
		log.info("Send Buffer Size: " + _client.getSendBufferSize());
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
				_client = new Socket(_host, _port);
				_rawo = new DataOutputStream(_client.getOutputStream());
				_rawi = new DataInputStream(_client.getInputStream());
				_addr = _client.getRemoteSocketAddress();
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
			_client.close();
		}
		catch (Throwable e)
		{
		}
	}

	public boolean isConnected()
	{
		return _client.isConnected();
	}

	public boolean isInputShutdown()
	{
		return _client.isInputShutdown();
	}

	public boolean isOutputShutdown()
	{
		return _client.isOutputShutdown();
	}

	public SocketAddress getInetAddress()
	{
		return _addr;
	}

	public String getAddress()
	{
		return _saddr;
	}
}
