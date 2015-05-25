package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BrokerMessage")
public class BrokerMessage
{
	@XmlElement(name = "Priority")
	public int priority;

	@XmlElement(name = "MessageId")
	public String messageId;

	@XmlElement(name = "CorrelationId")
	public String correlationId;

	@XmlElement(name = "Timestamp")
	public String timestamp;

	@XmlElement(name = "Expiration")
	public String expiration;

	@XmlElement(name = "DestinationName")
	public String destinationName;

	@XmlElement(name = "TextPayload")
	public String textPayload;

	public BrokerMessage()
	{
		messageId = "";
		timestamp = "";
		expiration = "";
		destinationName = "";
		textPayload = "";
	}
}
