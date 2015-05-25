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
 *         &lt;element name="RevokeTokenResult" type="{http://services.sapo.pt/definitions}RevokeTokenInfo"/>
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
		"revokeTokenResult"
})
@XmlRootElement(name = "RevokeTokenResponse")
public class RevokeTokenResponse
{

	@XmlElement(name = "RevokeTokenResult", required = true)
	protected RevokeTokenInfo revokeTokenResult;

	/**
	 * Gets the value of the revokeTokenResult property.
	 * 
	 * @return possible object is {@link RevokeTokenInfo }
	 * 
	 */
	public RevokeTokenInfo getRevokeTokenResult()
	{
		return revokeTokenResult;
	}

	/**
	 * Sets the value of the revokeTokenResult property.
	 * 
	 * @param value
	 *            allowed object is {@link RevokeTokenInfo }
	 * 
	 */
	public void setRevokeTokenResult(RevokeTokenInfo value)
	{
		this.revokeTokenResult = value;
	}

}
