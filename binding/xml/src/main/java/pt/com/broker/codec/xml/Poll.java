package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Poll")
public class Poll
{
	@XmlAttribute(name = "action-id")
	public String actionId;

	@XmlElement(name = "DestinationName")
	public String destinationName;

	public Poll()
	{
		destinationName = "";
	}
}
