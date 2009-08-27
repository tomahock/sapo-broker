package pt.com.gcs.messaging;

import java.net.SocketAddress;

/**
 * Connect is a Runnable type responsible for connecting to a specified address using GCS.connect().
 * 
 */

public class Connect implements Runnable
{
	private SocketAddress _address;

	public Connect(SocketAddress address)
	{
		_address = address;
	}

	public void run()
	{
		Gcs.connect(_address);
	}
}