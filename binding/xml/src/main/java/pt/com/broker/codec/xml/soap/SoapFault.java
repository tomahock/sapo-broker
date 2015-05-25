package pt.com.broker.codec.xml.soap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Fault")
public class SoapFault
{
	@XmlElement(name = "Code")
	public FaultCode faultCode;

	@XmlElement(name = "Reason")
	public FaultReason faultReason;

	public String detail = "";

	public SoapFault()
	{
		detail = "";
		faultCode = new FaultCode();
		faultReason = new FaultReason();
	}
}
