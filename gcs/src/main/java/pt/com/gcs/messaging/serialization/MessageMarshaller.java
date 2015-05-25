package pt.com.gcs.messaging.serialization;

import java.io.ObjectInputStream;

import org.caudexorigo.io.UnsynchronizedByteArrayInputStream;

import pt.com.gcs.messaging.BDBMessage;
import pt.com.gcs.messaging.InternalMessage;

public class MessageMarshaller
{
	private static BDBMessageMarshallerV2 marshallerV2 = new BDBMessageMarshallerV2();

	public static BDBMessage unmarshallBDBMessage(byte[] data)
	{
		try
		{
			ObjectInputStream oIn;
			oIn = new ObjectInputStream(new UnsynchronizedByteArrayInputStream(data));

			short version = oIn.readShort();

			BDBMessage result = null;

			if (version == 2)
			{
				result = marshallerV2.unmarshall(data);
			}
			else if (version == 1)
			{
				result = new BDBMessageMarshallerV1().unmarshall(data);
			}
			else
			{
				throw new RuntimeException("Can't deserialize version: " + version);
			}

			// if ((result != null) && (result.getMessage().getHeaders() != null) && result.getMessage().getAction().getActionType().equals(NetAction.ActionType.NOTIFICATION))
			// {
			// result.getMessage().getAction().getNotificationMessage().getMessage().setHeaders(result.getMessage().getHeaders());
			// }

			return result;
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	public static byte[] marshallBDBMessage(BDBMessage message)
	{
		return marshallerV2.marshall(message);
	}

	public static InternalMessage unmarshallInternalMessage(byte[] data) throws Throwable
	{
		return new InternalMessageMarshallerV1().unmarshall(data);
	}

	public static InternalMessage unmarshallInternalMessage(ObjectInputStream inputStream, short versionHint) throws Throwable
	{
		if (versionHint == 1)
		{
			return new InternalMessageMarshallerV1().unmarshall(inputStream);
		}
		return null;
	}

	public static InternalMessage unmarshallInternalMessage(ObjectInputStream inputStream) throws Throwable
	{
		return new InternalMessageMarshallerV1().unmarshall(inputStream);
	}
}