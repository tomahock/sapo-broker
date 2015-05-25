package pt.com.broker.codec.xml.soap;

import javax.xml.bind.annotation.XmlElement;

public class FaultReason
{
	@XmlElement(name = "Text")
	public String text;

	public FaultReason()
	{
		text = "";
	}
}
