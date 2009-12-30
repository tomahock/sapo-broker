package pt.com.gcs.messaging.serialization;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.gcs.messaging.BDBMessage;
import pt.com.gcs.messaging.InternalMessage;

public class MessageMarshaller
{
	private static Logger log = LoggerFactory.getLogger(MessageMarshaller.class);

	public static BDBMessage unmarshallBDBMessage(byte[] data) throws Throwable
	{
		try
		{
			return new BDBMessageMarshallerV1().unmarshall(data);
		}
		catch (Throwable t)
		{
			log.info("BDBMessage unmarshall failed using version 1, trying version 0.");
			try
			{
				BDBMessage message = new BDBMessageMarshallerV0().unmarshall(data);
				log.info("BDBMessage unmarshall using version 0 succeded.");
				return message;
			}
			catch (Throwable t2)
			{
				throw new Exception("Failed to unmarshall BDBMessage");
			}
		}
	}

	public static byte[] marshallBDBMessage(BDBMessage message) throws Throwable
	{
		if (message.getVersion() == 1)
		{
			return new BDBMessageMarshallerV1().marshall(message);
		}
		else if (message.getVersion() == 0)
		{
			return new BDBMessageMarshallerV0().marshall(message);
		}
		System.out.println("MessageMarshaller.marshallBDBMessage() returning null");
		return null;
	}

	public static InternalMessage unmarshallInternalMessage(byte[] data) throws Throwable
	{
		try
		{
			return new InternalMessageMarshallerV1().unmarshall(data);
		}
		catch (Throwable t)
		{
			log.info("InternalMessage unmarshall failed using version 1, trying version 0.");
			t.printStackTrace();
			try
			{
				return new InternalMessageMarshallerV0().unmarshall(data);
			}
			catch (Throwable t2)
			{
				throw new Exception("Failed to unmarshall InternalMessage");
			}
		}
	}

	public static byte[] marshallInternalMessage(InternalMessage internalMessage) throws Throwable
	{
		if (internalMessage.getVersion() == 1)
		{
			return new InternalMessageMarshallerV1().marshall(internalMessage);
		}
		else if (internalMessage.getVersion() == 0)
		{
			return new InternalMessageMarshallerV0().marshall(internalMessage);
		}
		return null;
	}

	public static void marshallInternalMessage(InternalMessage internalMessage, ObjectOutputStream objectOutupStream) throws Throwable
	{
		if (internalMessage.getVersion() == 1)
		{
			new InternalMessageMarshallerV1().marshall(internalMessage, objectOutupStream);
		}
		else if (internalMessage.getVersion() == 0)
		{
			new InternalMessageMarshallerV0().marshall(internalMessage, objectOutupStream);
		}
	}

	public static InternalMessage unmarshallInternalMessage(ObjectInputStream inputStream, short versionHint) throws Throwable
	{
		if (versionHint == 1)
		{
			return new InternalMessageMarshallerV1().unmarshall(inputStream);
		}
		else if (versionHint == 0)
		{
			return new InternalMessageMarshallerV0().unmarshall(inputStream);
		}
		return null;
	}

	public static InternalMessage unmarshallInternalMessage(ObjectInputStream inputStream) throws Throwable
	{
		try
		{
			return new InternalMessageMarshallerV1().unmarshall(inputStream);
		}
		catch (Throwable t)
		{
			log.info("InternalMessage unmarshall failed using version 1, trying version 0.");
			try
			{
				return new InternalMessageMarshallerV0().unmarshall(inputStream);
			}
			catch (Throwable t2)
			{
				throw new Exception("Failed to unmarshall InternalMessage");
			}
		}
	}

}
