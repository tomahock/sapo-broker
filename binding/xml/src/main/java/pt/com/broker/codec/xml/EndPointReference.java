package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlElement;


public class EndPointReference
{
    @XmlElement(name = "Address", namespace = "http://www.w3.org/2005/08/addressing")
	public String address;
}