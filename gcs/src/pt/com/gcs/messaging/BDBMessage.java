package pt.com.gcs.messaging;

import pt.com.broker.types.NetMessage;

/**
 * BDBMessage represents a message to be stored or retrieved from the database.
 */
public class BDBMessage
{
	public static final short CURRENT_VERSION = 2;
	private static final String SEPARATOR = "<#>";

	private NetMessage _message;
	private boolean _preferLocalConsumer;

	private byte[] _raw_packet;
	private long _reserve;
	private long _sequence;
	private short version = CURRENT_VERSION;

	public BDBMessage()
	{
	}

	public BDBMessage(NetMessage msg, long sequence, boolean preferLocalConsumer)
	{
		_preferLocalConsumer = preferLocalConsumer;
		_message = msg;
		_sequence = sequence;
		_reserve = 0L;
	}

	public BDBMessage(NetMessage msg, long sequence, boolean preferLocalConsumer, long reserveTimeout)
	{
		_preferLocalConsumer = preferLocalConsumer;
		_message = msg;
		_sequence = sequence;
		_reserve = reserveTimeout;
	}

	public NetMessage getMessage()
	{
		return _message;
	}

	public boolean getPreferLocalConsumer()
	{
		return _preferLocalConsumer;
	}

	public byte[] getRawPacket()
	{
		return _raw_packet;
	}

	public long getReserveTimeout()
	{
		return _reserve;
	}

	public long getSequence()
	{
		return _sequence;
	}

	public short getVersion()
	{
		return version;
	}

	public void setMessage(NetMessage internalMessage)
	{
		_message = internalMessage;
	}

	public void setPreferLocalConsumer(boolean localConsumer)
	{
		_preferLocalConsumer = localConsumer;
	}

	public void setRawPacket(byte[] raw_packet)
	{
		_raw_packet = raw_packet;
	}

	public void setReserveTimeout(long reserve)
	{
		_reserve = reserve;
	}

	public void setSequence(long sequence)
	{
		_sequence = sequence;
	}

	public void setVersion(short version)
	{
		this.version = version;
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder(100);
		buf.append(_message.toString());
		buf.append(SEPARATOR);
		buf.append(_sequence);
		buf.append(SEPARATOR);
		buf.append(_preferLocalConsumer);
		buf.append(SEPARATOR);
		buf.append(_reserve);

		return buf.toString();
	}
}