package pt.com.gcs.messaging.serialization;

import java.io.ObjectInputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.gcs.messaging.BDBMessage;
import pt.com.gcs.messaging.InternalMessage;

public class BDBMessageMarshallerV1 implements Codec<BDBMessage>
{
	private static final short VERSION = 1;
	private static final short CURRENT_VERSION = 2;

	private static Logger log = LoggerFactory.getLogger(BDBMessageMarshallerV1.class);

	@Override
	public byte[] marshall(BDBMessage bdbMessage) throws Throwable
	{
		// UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
		// ObjectOutputStream oout = new ObjectOutputStream(bout);
		//
		// oout.writeShort(bdbMessage.getVersion());
		//
		// oout.writeLong(bdbMessage.getSequence());
		// oout.writeBoolean(bdbMessage.getPreferLocalConsumer());
		// oout.writeLong(bdbMessage.getReserveTimeout());
		//
		// MessageMarshaller.marshallInternalMessage(bdbMessage.getMessage(), oout);
		//
		// oout.flush();
		//
		// return bout.toByteArray();

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
