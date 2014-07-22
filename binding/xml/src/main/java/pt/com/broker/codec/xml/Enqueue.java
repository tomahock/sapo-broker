package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Acknowledge")
public class Enqueue
{
    @XmlAttribute(name = "action-id")
	public String actionId;

    @XmlElement(name = "BrokerMessage")
	public BrokerMessage brokerMessage;

	public Enqueue()
	{
		brokerMessage = new BrokerMessage();
	}
}
