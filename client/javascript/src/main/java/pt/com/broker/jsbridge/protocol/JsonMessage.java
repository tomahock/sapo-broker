package pt.com.broker.jsbridge.protocol;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(value = false)
public class JsonMessage
{
	public enum MessageType
	{
		SUBSCRIBE, UNSUBSCRIBE, PUBLISH, NOTIFICATION, FAULT, RESPONSE
	};

	private MessageType action;
	private String channel;

	private String payload;

	public JsonMessage()
	{
	}

	public JsonMessage(MessageType action, String channel)
	{
		this(action, channel, null);
	}

	public JsonMessage(MessageType action, String channel, String payload)
	{
		this.action = action;
		this.channel = channel;
		this.payload = payload;
	}

	public MessageType getAction()
	{
		return action;
	}

	public String getChannel()
	{
		return channel;
	}

	public String getPayload()
	{
		return payload;
	}

	public void setAction(MessageType action)
	{
		this.action = action;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public void setPayload(String payload)
	{
		this.payload = payload;
	}
}