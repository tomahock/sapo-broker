package pt.com.gcs.messaging.serialization;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;
import org.caudexorigo.io.UnsynchronizedByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.messaging.BDBMessage;
import pt.com.gcs.messaging.InternalMessage;

public class BDBMessageMarshallerV1 implements Codec<BDBMessage>
{
	private static final short  VERSION = 1; 
	
	private static Logger log = LoggerFactory.getLogger(BDBMessageMarshallerV1.class);
	
	@Override
	public byte[] marshall(BDBMessage bdbMessage)  throws Throwable
	{
		UnsynchronizedByteArrayOutputStream bout = new UnsynchronizedByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		
		oout.writeShort(bdbMessage.getVersion());
		
		oout.writeLong(bdbMessage.getSequence());
		oout.writeBoolean(bdbMessage.getPreferLocalConsumer());
		oout.writeLong( bdbMessage.getReserveTimeout() );
		
		MessageMarshaller.marshallInternalMessage(bdbMessage.getMessage(), oout);
		
		oout.flush();
		
		return bout.toByteArray();
	}

	@Override
	public BDBMessage unmarshall(byte[] data) throws Throwable
	{
		BDBMessage message = new BDBMessage();
		
		ObjectInputStream oIn;
		oIn = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));
		
		short version = oIn.readShort(); 
		
		if(version  != VERSION )
		{
			String errorMessage = "Incorrect serialization version: " + version;
//			log.error(errorMessage);
			throw new Exception(errorMessage);
		}
		
		message.setVersion(version);
		
		message.setSequence(oIn.readLong());
		
		message.setPreferLocalConsumer(oIn.readBoolean());

		message.setReserveTimeout(oIn.readLong());
		
		message.setMessage( MessageMarshaller.unmarshallInternalMessage(oIn) );

		return message;
	}
	

}
