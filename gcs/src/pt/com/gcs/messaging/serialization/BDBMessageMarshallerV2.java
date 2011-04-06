package pt.com.gcs.messaging.serialization;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.codec.protobuf.ProtoBufBindingSerializer;
import pt.com.broker.types.NetMessage;
import pt.com.gcs.messaging.BDBMessage;

public class BDBMessageMarshallerV2 implements Codec<BDBMessage>
{
	private static Logger log = LoggerFactory.getLogger(BDBMessageMarshallerV2.class);
	private static final ProtoBufBindingSerializer serializer = new ProtoBufBindingSerializer();

	public byte[] marshall(BDBMessage bdbMessage) throws Throwable
	{
		UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);

		oout.writeShort(bdbMessage.getVersion());

		oout.writeLong(bdbMessage.getSequence());
		oout.writeBoolean(bdbMessage.getPreferLocalConsumer());
		oout.writeLong(bdbMessage.getReserveTimeout());
		serializer.marshal(bdbMessage.getMessage(), oout);

		oout.flush();

		return bout.toByteArray();
	}

	@Override
	public BDBMessage unmarshall(byte[] data) throws Throwable
	{
		BDBMessage message = new BDBMessage();

		ObjectInputStream oIn = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));

		short version = oIn.readShort();

		if (version != 2)
		{
			String errorMessage = "Incorrect serialization version: " + version;
			throw new RuntimeException(errorMessage);
		}

		message.setVersion(version);
		message.setSequence(oIn.readLong());
		message.setPreferLocalConsumer(oIn.readBoolean());
		message.setReserveTimeout(oIn.readLong());

		NetMessage nmsg = serializer.unmarshal(oIn);

		message.setMessage(nmsg);

		return message;
	}
}