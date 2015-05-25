package pt.sapo.services.definitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetInfoCardClaimsResult" type="{http://services.sapo.pt/definitions}Claims" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"getInfoCardClaimsResult"
})
@XmlRootElement(name = "GetInfoCardClaimsResponse")
public class GetInfoCardClaimsResponse
{

	@XmlElement(name = "GetInfoCardClaimsResult")
	protected Claims getInfoCardClaimsResult;

	/**
	 * Gets the value of the getInfoCardClaimsResult property.
	 * 
	 * @return possible object is {@link Claims }
	 * 
	 */
	public Claims getGetInfoCardClaimsResult()
	{
		return getInfoCardClaimsResult;
	}

	/**
	 * Sets the value of the getInfoCardClaimsResult property.
	 * 
	 * @param value
	 *            allowed object is {@link Claims }
	 * 
	 */
	public void setGetInfoCardClaimsResult(Claims value)
	{
		this.getInfoCardClaimsResult = value;
	}

}
