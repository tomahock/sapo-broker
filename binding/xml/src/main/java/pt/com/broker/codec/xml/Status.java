package pt.com.broker.codec.xml;

import java.util.Date;

import org.caudexorigo.text.DateUtil;

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
		timestamp = DateUtil.formatISODate(new Date());
	}
}
