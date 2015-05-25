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
 *         &lt;element name="ListRevokableTokensResult" type="{http://services.sapo.pt/definitions}RevokableTokens"/>
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
		"listRevokableTokensResult"
})
@XmlRootElement(name = "ListRevokableTokensResponse")
public class ListRevokableTokensResponse
{

	@XmlElement(name = "ListRevokableTokensResult", required = true)
	protected RevokableTokens listRevokableTokensResult;

	/**
	 * Gets the value of the listRevokableTokensResult property.
	 * 
	 * @return possible object is {@link RevokableTokens }
	 * 
	 */
	public RevokableTokens getListRevokableTokensResult()
	{
		return listRevokableTokensResult;
	}

	/**
	 * Sets the value of the listRevokableTokensResult property.
	 * 
	 * @param value
	 *            allowed object is {@link RevokableTokens }
	 * 
	 */
	public void setListRevokableTokensResult(RevokableTokens value)
	{
		this.listRevokableTokensResult = value;
	}

}
