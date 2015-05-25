package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "Notification")
public class Notification
{
	@XmlAttribute(name = "action-id")
	@XmlJavaTypeAdapter(EmptyStringAdapter.class)
	public String actionId;

	@XmlElement(name = "BrokerMessage")
	public BrokerMessage brokerMessage;

	public Notification()
	{
		brokerMessage = new BrokerMessage();
	}

}
