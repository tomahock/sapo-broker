package pt.com.broker.types;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a client message payload.
 * 
 */

public class NetBrokerMessage
{
	private static Logger log = LoggerFactory.getLogger(NetBrokerMessage.class);

	private String messageId = "";
	private byte[] payload;
	private long expiration = -1;
	private long timestamp = -1;

	private long deferredDelivery = -1; // -1 - initial value (don't know yet if the delivery is deferred), 0 - the delivery is not deferred, > 0 time to deliver

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

	public long getDeferredDelivery()
	{
		if (deferredDelivery == -1)
		{
			String deferredDeliveryStr = getHeaders().get(Headers.DEFERRED_DELIVERY);
			if (StringUtils.isBlank(deferredDeliveryStr))
			{
				this.deferredDelivery = 0;
			}
			else
			{
				try
				{
					long value = Long.parseLong(deferredDeliveryStr);
					if (value > 0)
					{
						this.deferredDelivery = value;
					}
				}
				catch (Exception e)
				{
					// This shouldn't happen.
					log.warn(String.format("Invalid value (%s) for '%s' header."), deferredDeliveryStr, Headers.DEFERRED_DELIVERY);
					throw new RuntimeException(e);
				}
			}
		}

		return deferredDelivery;
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

		message.getDeferredDelivery();

		return message;
	}

	public void setHeaders(Map<String, String> headers)
	{
		this.headers = headers;
	}

	public Map<String, String> getHeaders()
	{
		if (headers == null)
		{
			headers = new HashMap<String, String>();
		}
		return headers;
	}

	public void addHeader(String header, String value)
	{
		getHeaders().put(header, value);
	}

	public void addAllHeaders(Map<String, String> headers)
	{
		getHeaders().putAll(headers);
	}
}
