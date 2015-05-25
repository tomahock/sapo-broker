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
 *         &lt;element name="GetRolesResult" type="{http://services.sapo.pt/definitions}ESBRoles" minOccurs="0"/>
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
		"getRolesResult"
})
@XmlRootElement(name = "GetRolesResponse")
public class GetRolesResponse
{

	@XmlElement(name = "GetRolesResult")
	protected ESBRoles getRolesResult;

	/**
	 * Gets the value of the getRolesResult property.
	 * 
	 * @return possible object is {@link ESBRoles }
	 * 
	 */
	public ESBRoles getGetRolesResult()
	{
		return getRolesResult;
	}

	/**
	 * Sets the value of the getRolesResult property.
	 * 
	 * @param value
	 *            allowed object is {@link ESBRoles }
	 * 
	 */
	public void setGetRolesResult(ESBRoles value)
	{
		this.getRolesResult = value;
	}

}
