package pt.com.broker.codec.xml;

import org.caudexorigo.time.ISO8601;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Status")
public class Status
{
    @XmlElement(name = "Message")
	public String message;

    @XmlElement(name = "Timestamp")
	public String timestamp;

	public Status()
	{
		message = ":)";
		timestamp = ISO8601.format(new Date());
	}
}
