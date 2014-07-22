package pt.com.broker.codec.xml.soap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class FaultCode
{
    @XmlElement(name = "Value")
	public String value;

    @XmlElement(name = "Subcode")
	public FaultCode subcode;

	public FaultCode()
	{
		value = "";
	}
}
