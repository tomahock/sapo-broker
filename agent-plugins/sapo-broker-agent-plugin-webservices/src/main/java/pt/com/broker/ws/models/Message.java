package pt.com.broker.ws.models;

import java.util.HashMap;
import java.util.Map;

import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message
{

	public static final String JSON_PROP_MESSAGE_ID = "id";
	public static final String JSON_PROP_MESSAGE_HEADERS = "headers";
	public static final String JSON_PROP_MESSAGE_TYPE = "type";
	public static final String JSON_PROP_DESTINATION = "destination";
	public static final String JSON_PROP_DESTINATION_TYPE = "destination_type";

	@JsonProperty(JSON_PROP_MESSAGE_ID)
	private String messageId;
	@JsonProperty(JSON_PROP_MESSAGE_HEADERS)
	private Map<String, String> messageHeaders;
	@JsonProperty(JSON_PROP_MESSAGE_TYPE)
	private String messageType;
	@JsonProperty(JSON_PROP_DESTINATION_TYPE)
	private NetAction.DestinationType destinationType;
	@JsonProperty(JSON_PROP_DESTINATION)
	private String destination;

	public Message()
	{
		this.messageId = new String("");
		this.messageHeaders = new HashMap<String, String>();
		this.messageType = new String("");
		this.destination = new String("");
		this.destinationType = null;
	}

	public Message(String messageId, Map<String, String> messageHeaders, String messageType, String destination, DestinationType destinationType)
	{
		this.messageId = messageId;
		this.messageHeaders = messageHeaders;
		this.messageType = messageType;
		this.destination = destination;
		this.destinationType = destinationType;
	}

	public String getMessageId()
	{
		return messageId;
	}

	public Map<String, String> getMessageHeaders()
	{
		return messageHeaders;
	}

	public String getMessageType()
	{
		return messageType;
	}

	public String getDestination()
	{
		return destination;
	}

	public NetAction.DestinationType getDestinationType()
	{
		return destinationType;
	}

}
