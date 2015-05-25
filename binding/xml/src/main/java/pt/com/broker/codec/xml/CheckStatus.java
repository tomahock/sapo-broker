package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CheckStatus")
public class CheckStatus
{
	@XmlAttribute(name = "action-id")
	public String actionId;
}
