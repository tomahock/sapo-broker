package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Acknowledge")
public class Acknowledge
{
    @XmlAttribute(name = "action-id")
	public String actionId;

    @XmlElement(name = "DestinationName", required = true)
	public String destinationName;

    @XmlElement(name = "MessageId",required = true)
	public String messageId;
}
