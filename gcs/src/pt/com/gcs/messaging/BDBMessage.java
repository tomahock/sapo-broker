package pt.com.gcs.messaging;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;

import org.caudexorigo.io.UnsynchByteArrayInputStream;

/**
 * BDBMessage represents a message to be stored or retrieved from the database.
 * 
 */

public class BDBMessage implements Externalizable
{

	private long _sequence;
	private boolean _preferLocalConsumer;
	private long _reserve;
	private int _deliveryCount;
	private InternalMessage _message;
	private static final String SEPARATOR = "<#>";

	private BDBMessage()
	{
	}

	public BDBMessage(InternalMessage msg, long sequence, int deliveryCount, boolean preferLocalConsumer)
	{
		_deliveryCount = deliveryCount;
		_preferLocalConsumer = preferLocalConsumer;
		_message = msg;
		_sequence = sequence;
		_reserve = 0L;
	}

	public boolean getPreferLocalConsumer()
	{
		return _preferLocalConsumer;
	}

	public void setPreferLocalConsumer(boolean localConsumer)
	{
		_preferLocalConsumer = localConsumer;
	}

	public long getSequence()
	{
		return _sequence;
	}

	public long getPollReserveTimeout()
	{
		return _reserve;
	}

	public int getDeliveryCount()
	{
		return _deliveryCount;
	}

	public InternalMessage getMessage()
	{
		return _message;
	}

	public void setDeliveryCount(int count)
	{
		_deliveryCount = count;
	}

	public void setPollReserveTimeout(long reserve)
	{
		_reserve = reserve;
	}

	public void readExternal(ObjectInput oin) throws IOException, ClassNotFoundException
	{
		_sequence = oin.readLong();
		_preferLocalConsumer = oin.readBoolean();
		_deliveryCount = oin.readInt();
		_reserve = oin.readLong();
		InternalMessage m = new InternalMessage();

		m.readExternal(oin);
		_message = m;

	}

	public void writeExternal(ObjectOutput oout) throws IOException
	{
		oout.writeLong(_sequence);
		oout.writeBoolean(_preferLocalConsumer);
		oout.writeInt(_deliveryCount);
		oout.writeLong(_reserve);
		_message.writeExternal(oout);
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

	protected static BDBMessage fromByteArray(byte[] buf) throws IOException, ClassNotFoundException
	{
		BDBMessage bm = new BDBMessage();
		ObjectInputStream oIn;

		oIn = new ObjectInputStream(new UnsynchByteArrayInputStream(buf));
		bm.readExternal(oIn);
		return bm;
	}

}
