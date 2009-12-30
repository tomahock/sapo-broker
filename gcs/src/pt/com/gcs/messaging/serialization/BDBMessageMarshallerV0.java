package pt.com.gcs.messaging.serialization;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.messaging.BDBMessage;
import pt.com.gcs.messaging.InternalMessage;

public class BDBMessageMarshallerV0 implements Codec<BDBMessage>
{

	private static Logger log = LoggerFactory.getLogger(BDBMessageMarshallerV0.class);
	
	@Override
	public byte[] marshall(BDBMessage bdbMessage)  throws Throwable
	{
		log.warn("Using version ZERO to serialize BDBMessage objects. This shouldn't happen.");
		
		UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		
		
		oout.writeLong(bdbMessage.getSequence());
		oout.writeBoolean(bdbMessage.getPreferLocalConsumer());
		oout.writeLong( bdbMessage.getReserveTimeout() );
		
		byte[] marshalledInternalMessage = MessageMarshaller.marshallInternalMessage(bdbMessage.getMessage());
		
		oout.write(marshalledInternalMessage);
		
		oout.flush();
		
		return bout.toByteArray();
	}

	@Override
	public BDBMessage unmarshall(byte[] data) throws Throwable
	{
		BDBMessage message = new BDBMessage();
		
		ObjectInputStream oIn;
		oIn = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));
		
		message.setSequence(oIn.readLong());
		
		message.setPreferLocalConsumer(oIn.readBoolean());

		message.setReserveTimeout(oIn.readLong());
		
		InternalMessage internalMessage = MessageMarshaller.unmarshallInternalMessage(oIn, (short)0);
		
		message.setMessage(internalMessage);
		
		message.setVersion(BDBMessage.CURRENT_VERSION);  // Set version to "current version" 
				
		return message;
	}

}
