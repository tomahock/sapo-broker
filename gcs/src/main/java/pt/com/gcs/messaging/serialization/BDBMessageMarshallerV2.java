package pt.com.gcs.messaging.serialization;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.gcs.messaging.BDBMessage;

public class BDBMessageMarshallerV2 implements Codec<BDBMessage>
{
	private static final ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();

	public byte[] marshall(BDBMessage bdbMessage)
	{
		try
		{
			UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(bout);

			oout.writeShort(bdbMessage.getVersion());

			oout.writeLong(bdbMessage.getSequence());
			oout.writeBoolean(bdbMessage.getPreferLocalConsumer());
			oout.writeLong(bdbMessage.getReserveTimeout());

			if (bdbMessage.getRawPacket() == null)
			{
				serializer.marshal(bdbMessage.getMessage(), oout);
			}
			else
			{
				oout.write(bdbMessage.getRawPacket());
			}

			oout.flush();

			return bout.toByteArray();
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	@Override
	public BDBMessage unmarshall(byte[] data) throws Throwable
	{
		BDBMessage message = new BDBMessage();

		ObjectInputStream oIn = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));

		short version = oIn.readShort();

		if (version != 2)
		{
			String errorMessage = String.format("Incorrect serialization version: ", version);
			throw new RuntimeException(errorMessage);
		}

		message.setVersion(version);
		message.setSequence(oIn.readLong());
		message.setPreferLocalConsumer(oIn.readBoolean());
		message.setReserveTimeout(oIn.readLong());

		//byte[] buf = new byte[oIn.available()];

	
		NetMessage nmsg = serializer.unmarshal(oIn);

		//message.setRawPacket(buf);
		message.setMessage(nmsg);

		return message;
	}
}