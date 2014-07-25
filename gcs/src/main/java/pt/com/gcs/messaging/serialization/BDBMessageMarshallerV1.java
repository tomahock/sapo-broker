package pt.com.gcs.messaging.serialization;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.gcs.messaging.BDBMessage;
import pt.com.gcs.messaging.InternalMessage;

import java.io.ObjectInputStream;

public class BDBMessageMarshallerV1 implements Codec<BDBMessage>
{
	private static final short VERSION = 1;
	private static final short CURRENT_VERSION = 2;

	@Override
	public byte[] marshall(BDBMessage bdbMessage) throws Throwable
	{
		throw new RuntimeException("BDBMessageMarshallerV1 is deprecated");
	}

	@Override
	public BDBMessage unmarshall(byte[] data) throws Throwable
	{
		BDBMessage message = new BDBMessage();

		ObjectInputStream oIn;
		oIn = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));

		short version = oIn.readShort();

		if (version != VERSION)
		{
			String errorMessage = "Incorrect serialization version: " + version;
			// log.error(errorMessage);
			throw new Exception(errorMessage);
		}

		message.setVersion(version);

		message.setSequence(oIn.readLong());

		message.setPreferLocalConsumer(oIn.readBoolean());

		message.setReserveTimeout(oIn.readLong());

		InternalMessage imsg = MessageMarshaller.unmarshallInternalMessage(oIn);

		NetBrokerMessage brkMsg = imsg.getContent();

		brkMsg.setExpiration(imsg.getExpiration());
		brkMsg.setMessageId(imsg.getMessageId());
		brkMsg.setTimestamp(imsg.getTimestamp());

		NetNotification notification = new NetNotification(imsg.getDestination(), DestinationType.TOPIC, brkMsg, imsg.getDestination());

		NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
		naction.setNotificationMessage(notification);

		NetMessage nmsg = new NetMessage(naction);

		message.setMessage(nmsg);

		// Set current Version!!
		message.setVersion(CURRENT_VERSION);

		return message;
	}
}