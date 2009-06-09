package pt.com.gcs.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import pt.com.gcs.messaging.InternalMessage;

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
			InternalMessage msg = new InternalMessage();
			msg.readExternal(oIn);

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
			msg.writeExternal(objectOutputStream);
			objectOutputStream.flush();
			objectOutputStream.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}