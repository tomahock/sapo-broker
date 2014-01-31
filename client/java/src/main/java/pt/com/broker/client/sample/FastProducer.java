package pt.com.broker.client.sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.BindingSerializer;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;

public class FastProducer
{
	private String _host;

	private int _port;

	private Socket _client;

	private DataOutputStream _rawo = null;

	private DataInputStream _rawi = null;

	public FastProducer(String host, int port)
	{
		_host = host;
		_port = port;

		try
		{
			_client = new Socket(_host, _port);
			System.out.printf("Socket -> sendBufferSize: %s%n", _client.getSendBufferSize());
			_client.setSendBufferSize(_client.getSendBufferSize() * 4);
			System.out.printf("Socket -> sendBufferSize: %s%n", _client.getSendBufferSize());
			_rawo = new DataOutputStream(_client.getOutputStream());
			_rawi = new DataInputStream(_client.getInputStream());
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public int messageLenght() throws IOException
	{
		return _rawi.readInt();
	}

	public void readfully(byte[] buf) throws IOException
	{
		_rawi.readFully(buf);
	}

	private void closeQuietly()
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

	public void sendBatch(final String destinationName, final String destinationType, int batchSize)
	{
		try
		{
			byte[] message = getMesssage(destinationName, destinationType);
			for (int i = 0; i < batchSize; i++)
			{
				_rawo.writeShort(1);
				_rawo.writeShort(0);
				_rawo.writeInt(message.length);
				_rawo.write(message);

				if (i % 5000 == 0)
				{
					System.out.printf("Sent messages: %s%n", i);
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.exit(-1);
		}
		closeQuietly();
	}

	private byte[] getMesssage(String destinationName, String destinationType)
	{
		// int msize = 1024;
		int msize = 25 * 1024;

		NetBrokerMessage msg = new NetBrokerMessage(RandomStringUtils.randomAlphabetic(msize));

		BindingSerializer serializer = new ProtoBufBindingSerializer();

		byte[] encodedMsg;

		if (destinationType.equals("TOPIC"))
		{
			NetPublish publish = new NetPublish(destinationName, NetAction.DestinationType.TOPIC, msg);
			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage message = new NetMessage(action);

			encodedMsg = serializer.marshal(message);

		}
		else if (destinationType.equals("QUEUE"))
		{
			NetPublish publish = new NetPublish(destinationName, NetAction.DestinationType.QUEUE, msg);
			NetAction action = new NetAction(ActionType.PUBLISH);
			action.setPublishMessage(publish);

			NetMessage message = new NetMessage(action);

			encodedMsg = serializer.marshal(message);
		}
		else
		{
			throw new IllegalArgumentException("Not a valid destination type!");
		}

		return encodedMsg;
	}

	public static void main(String[] args) throws Throwable
	{
		final String host = "172.17.1.102";
		final int port = 3323;

		FastProducer fpb = new FastProducer(host, port);

		System.out.println("************* Start sendLoop() ******************");

		long start, stop;
		start = System.currentTimeMillis();

		fpb.sendBatch("/queue/old", "QUEUE", 10000);

		stop = System.currentTimeMillis();
		double duration = ((double) (stop - start)) / 1000;

		System.out.printf("Time for sending: %6.2f seconds.%n", duration);

		System.exit(0);

	}

}