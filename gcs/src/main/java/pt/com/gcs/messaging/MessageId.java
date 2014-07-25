package pt.com.gcs.messaging;

import org.caudexorigo.cryto.MD5;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MessageId
{
	private static final String BASE_MESSAGE_ID = MD5.getHashString(UUID.randomUUID().toString());
	private static final AtomicLong SEQ = new AtomicLong(0L);

	public static String getBaseMessageId()
	{
		return BASE_MESSAGE_ID + "#";
	}

	public static String getMessageId()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseMessageId());
		sb.append(SEQ.incrementAndGet());
		return sb.toString();
	}
}
