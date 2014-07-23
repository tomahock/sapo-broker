package pt.com.broker.codec.xml.soap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Envelope" , namespace = "http://www.w3.org/2003/05/soap-envelope")
public class SoapEnvelope
{

    @XmlElement(required = false, name = "Header")
    public SoapHeader header;

    @XmlElement(required = true, name = "Body")
	public SoapBody body;


	public SoapEnvelope()
	{
		body = new SoapBody();
		header = new SoapHeader();
	}
}
