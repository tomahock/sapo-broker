package pt.com.broker.codec.xml.soap;

import pt.com.broker.codec.xml.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class SoapBody
{
    @XmlElement(required = false, name = "Fault")
	public SoapFault fault;

    @XmlElement(required = false, name = "Notify", namespace = "http://services.sapo.pt/broker")
	public Notify notify;

    @XmlElement(required = false, name = "Acknowledge", namespace = "http://services.sapo.pt/broker")
	public Acknowledge acknowledge;

    @XmlElement(required = false , name="Unsubscribe", namespace = "http://services.sapo.pt/broker")
	public Unsubscribe unsubscribe;

    @XmlElement(required = false, name="Enqueue", namespace = "http://services.sapo.pt/broker")
	public Enqueue enqueue;

    @XmlElement(required = false, name="Poll", namespace = "http://services.sapo.pt/broker")
	public Poll poll;

    @XmlElement(required = false , name="Notification", namespace = "http://services.sapo.pt/broker")
	public Notification notification;

    @XmlElement(required = false, name="Publish", namespace = "http://services.sapo.pt/broker")
	public Publish publish;

    @XmlElement(required = false, name = "CheckStatus", namespace = "http://services.sapo.pt/broker")
	public CheckStatus checkStatus;

    @XmlElement(required = false, name = "Status", namespace = "http://services.sapo.pt/broker")
	public Status status;

    @XmlElement(required = false, name = "Accepted", namespace = "http://services.sapo.pt/broker")
	public Accepted accepted;

}
