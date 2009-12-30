package pt.com.gcs.io;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import pt.com.gcs.messaging.InternalMessage;
import pt.com.gcs.messaging.serialization.MessageMarshaller;

/**
 * InternalMessage serialization helper that reads from and writes to Input and Output Streams.
 * 
 */

public class SerializerHelper
{
	public static InternalMessage fromStream(InputStream in)
	{
		try
		{
			ObjectInputStream oIn = new ObjectInputStream(in);
			
			InternalMessage msg = MessageMarshaller.unmarshallInternalMessage(oIn);

			return msg;
		}
		catch (Throwable e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void toStream(InternalMessage msg, OutputStream out)
	{
		try
		{
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
			MessageMarshaller.marshallInternalMessage(msg, objectOutputStream);
			objectOutputStream.flush();
			objectOutputStream.close();
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
}