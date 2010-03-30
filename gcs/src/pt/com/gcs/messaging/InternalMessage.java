package pt.com.gcs.messaging;

import org.caudexorigo.text.StringUtils;

import pt.com.broker.types.DeliverableMessage;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.gcs.conf.GcsInfo;

/**
 * InternalMessage is the internal representation of a message. It contains a NetBrokerMessage and other fields related with the original message. <br/>
 * It's used for storage and passing between agents.
 */
public class InternalMessage implements DeliverableMessage
{

	public static final short CURRENT_VERSION = 1;

	private static final long serialVersionUID = -3656321513130930115L;
	public static final int DEFAULT_PRIORITY = 4;
	private static final long DEFAULT_EXPIRY;// = 1000L * 3600L * 24L * 7L; // 7days
	private static final String SEPARATOR = "<#>";

	private String id;
	private NetBrokerMessage content;
	private String destination;
	private String correlationId;
	private int priority = DEFAULT_PRIORITY;
	private String sourceApp = "Undefined Source";
	private long timestamp = System.currentTimeMillis();
	private long expiration = timestamp + DEFAULT_EXPIRY;
	private pt.com.gcs.messaging.MessageType type = pt.com.gcs.messaging.MessageType.UNDEF;
	private boolean isFromRemotePeer = false;
	private String publishingAgent = GcsInfo.getAgentName(); // Agent through which the message entered the messaging system

	private short version = CURRENT_VERSION;

	static
	{
		DEFAULT_EXPIRY = GcsInfo.getMessageStorageTime();
	}

	private void checkArg(String value)
	{
		if (StringUtils.isBlank(value))
		{
			throw new IllegalArgumentException("Invalid argument. Message initializers must not empty");
		}
	}

	private void checkArg(Object value)
	{
		if (value == null)
		{
			throw new IllegalArgumentException("Invalid argument. Message initializers must not be null");
		}
	}

	public InternalMessage()
	{

		setId(MessageId.getMessageId());
	}

	public InternalMessage(String destination, NetBrokerMessage content)
	{
		checkArg(destination);
		checkArg(content);
		this.content = content;
		this.destination = destination;
		setId(MessageId.getMessageId());
	}

	public InternalMessage(String id, String destination, NetBrokerMessage content)
	{
		checkArg(destination);
		checkArg(content);
		checkArg(id);
		this.content = content;
		this.destination = destination;
		this.setId(id);
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}

	public String getMessageId()
	{
		return getId();
	}

	public void setMessageId(String id)
	{
		this.setId(id);
	}

	public NetBrokerMessage getContent()
	{
		return content;
	}

	public void setContent(NetBrokerMessage content)
	{
		this.content = content;
	}

	public String getCorrelationId()
	{
		return correlationId;
	}

	public void setCorrelationId(String cid)
	{
		if (StringUtils.isNotBlank(cid))
		{
			correlationId = cid;
		}
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	public String getSourceApp()
	{
		return sourceApp;
	}

	public void setSourceApp(String sourceApp)
	{
		this.sourceApp = sourceApp;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	public void setExpiration(long expiration)
	{
		this.expiration = expiration;
	}

	public long getExpiration()
	{
		return expiration;
	}

	public void setType(MessageType type)
	{
		this.type = type;
	}

	public pt.com.gcs.messaging.MessageType getType()
	{
		return type;
	}

	public void setFromRemotePeer(boolean isFromRemotePeer)
	{
		this.isFromRemotePeer = isFromRemotePeer;
	}

	public boolean isFromRemotePeer()
	{
		return isFromRemotePeer;
	}

	public void setPublishingAgent(String publishingAgent)
	{
		this.publishingAgent = publishingAgent;
	}

	public String getPublishingAgent()
	{
		return publishingAgent;
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder(100);
		buf.append(version);
		buf.append(SEPARATOR);
		buf.append(getContent());
		buf.append(SEPARATOR);
		buf.append(getCorrelationId());
		buf.append(SEPARATOR);
		buf.append(getDestination());
		buf.append(SEPARATOR);
		buf.append(getMessageId());
		buf.append(SEPARATOR);
		buf.append(getPublishingAgent());
		buf.append(SEPARATOR);
		buf.append(getPriority());
		buf.append(SEPARATOR);
		buf.append(getSourceApp());
		buf.append(SEPARATOR);
		buf.append(getTimestamp());
		buf.append(SEPARATOR);
		buf.append(getExpiration());
		buf.append(SEPARATOR);
		buf.append(getType().getValue());

		return buf.toString();
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getId()
	{
		return id;
	}

	public void setVersion(short version)
	{
		this.version = version;
	}

	public short getVersion()
	{
		return version;
	}

}
