package pt.com.broker.types;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a client message payload.
 * 
 */

public class NetBrokerMessage
{
	private String messageId = "";
	private byte[] payload;
	private long expiration = -1;
	private long timestamp = -1;
	
	private Map<String, String> headers;

	public NetBrokerMessage(String payload)
	{
		this.payload = payload.getBytes();
	}
	
	public NetBrokerMessage(byte[] payload)
	{
		this.payload = payload;
	}

	public void setMessageId(String messageId)
	{
		this.messageId = messageId;
	}

	public String getMessageId()
	{
		return messageId;
	}

	public byte[] getPayload()
	{
		return payload;
	}

	public void setExpiration(long expiration)
	{
		this.expiration = expiration;
	}

	public long getExpiration()
	{
		return expiration;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	public long getTimestamp()
	{
		return timestamp;
	}
	
	

	// read and write are used by InternalMessage serialization

	public void write(ObjectOutput out) throws IOException
	{
		out.writeUTF(messageId);
		out.writeLong(expiration);
		out.writeLong(timestamp);
		out.writeInt(payload.length);
		out.write(payload);
	}

	public static NetBrokerMessage read(ObjectInput in) throws IOException
	{
		String mid = in.readUTF();
		long exp = in.readLong();
		long ts = in.readLong();
		int size = in.readInt();
		byte[] content = new byte[size];
		in.readFully(content, 0, size);

		NetBrokerMessage message = new NetBrokerMessage(content);
		message.setMessageId(mid);
		message.setExpiration(exp);
		message.setTimestamp(ts);
		return message;
	}

	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}
	
	public void addHeader(String header, String value)
	{
		if(headers == null)
		{
			headers = new HashMap<String, String>();
		}
		headers.put(header, value);
	}
}
