package pt.com.broker.codec.xml.soap;

import pt.com.broker.codec.xml.EndPointReference;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class SoapHeader
{

	// wsa* -> ws-addressing fields;

    @XmlElement(name = "MessageID" , namespace = "http://www.w3.org/2005/08/addressing")
	public String wsaMessageID;

    @XmlElement(name = "RelatesTo" , namespace = "http://www.w3.org/2005/08/addressing")
	public String wsaRelatesTo;

    @XmlElement(name = "To" , namespace = "http://www.w3.org/2005/08/addressing")
	public String wsaTo;

    @XmlElement(name = "Action" , namespace = "http://www.w3.org/2005/08/addressing")
	public String wsaAction;

    @XmlElement(name = "From" , namespace = "http://www.w3.org/2005/08/addressing")
	public EndPointReference wsaFrom;

    @XmlElement(name = "ReplyTo" , namespace = "http://www.w3.org/2005/08/addressing")
	public EndPointReference wsaReplyTo;

    @XmlElement(name = "FaultTo" , namespace = "http://www.w3.org/2005/08/addressing")
	public EndPointReference wsaFaultTo;

}
