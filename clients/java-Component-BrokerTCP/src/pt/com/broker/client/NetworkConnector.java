package pt.com.broker.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkConnector
{
	private static final Logger log = LoggerFactory.getLogger(NetworkConnector.class);

	private final String _host;
	private final int _port;

	private Socket _client;
	private DataInputStream _rawi = null;
	private DataOutputStream _rawo = null;
	private SocketAddress _addr;
	private String _saddr;

	public NetworkConnector(String host, int port) throws UnknownHostException, IOException
	{
		_host = host;
		_port = port;

		setSocket(new Socket(_host, _port));
		getSocket().setSoTimeout(0);
		_rawo = new DataOutputStream(getSocket().getOutputStream());
		_rawi = new DataInputStream(getSocket().getInputStream());

		_addr = getSocket().getRemoteSocketAddress();
		_saddr = _addr.toString();

		log.info("Receive Buffer Size: " + getSocket().getReceiveBufferSize());
		log.info("Send Buffer Size: " + getSocket().getSendBufferSize());
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
