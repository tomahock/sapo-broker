package pt.sapo.services.definitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Aggregator type that contains information about the authentication response: primary identifier, token additional information and user
 * 
 * <p>
 * Java class for UserInfo complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PrimaryId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ESBRoles" type="{http://services.sapo.pt/definitions}ESBRoles" minOccurs="0"/>
 *         &lt;element name="ESBToken" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Group" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBCredentialsStore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AccountNumbers" type="{http://services.sapo.pt/definitions}AccountNumbers" minOccurs="0"/>
 *         &lt;element name="Attributes" type="{http://services.sapo.pt/definitions}ArrayOfAttribute" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserInfo", propOrder = {
		"primaryId",
		"esbRoles",
		"esbToken",
		"group",
		"esbCredentialsStore",
		"accountNumbers",
		"attributes"
})
public class UserInfo
{

	@XmlElement(name = "PrimaryId", required = true)
	protected String primaryId;
	@XmlElement(name = "ESBRoles")
	protected ESBRoles esbRoles;
	@XmlElement(name = "ESBToken", required = true)
	protected String esbToken;
	@XmlElement(name = "Group")
	protected String group;
	@XmlElement(name = "ESBCredentialsStore")
	protected String esbCredentialsStore;
	@XmlElement(name = "AccountNumbers")
	protected AccountNumbers accountNumbers;
	@XmlElement(name = "Attributes")
	protected ArrayOfAttribute attributes;

	/**
	 * Gets the value of the primaryId property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPrimaryId()
	{
		return primaryId;
	}

	/**
	 * Sets the value of the primaryId property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPrimaryId(String value)
	{
		this.primaryId = value;
	}

	/**
	 * Gets the value of the esbRoles property.
	 * 
	 * @return possible object is {@link ESBRoles }
	 * 
	 */
	public ESBRoles getESBRoles()
	{
		return esbRoles;
	}

	/**
	 * Sets the value of the esbRoles property.
	 * 
	 * @param value
	 *            allowed object is {@link ESBRoles }
	 * 
	 */
	public void setESBRoles(ESBRoles value)
	{
		this.esbRoles = value;
	}

	/**
	 * Gets the value of the esbToken property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getESBToken()
	{
		return esbToken;
	}

	/**
	 * Sets the value of the esbToken property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setESBToken(String value)
	{
		this.esbToken = value;
	}

	/**
	 * Gets the value of the group property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * Sets the value of the group property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setGroup(String value)
	{
		this.group = value;
	}

	/**
	 * Gets the value of the esbCredentialsStore property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getESBCredentialsStore()
	{
		return esbCredentialsStore;
	}

	/**
	 * Sets the value of the esbCredentialsStore property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setESBCredentialsStore(String value)
	{
		this.esbCredentialsStore = value;
	}

	/**
	 * Gets the value of the accountNumbers property.
	 * 
	 * @return possible object is {@link AccountNumbers }
	 * 
	 */
	public AccountNumbers getAccountNumbers()
	{
		return accountNumbers;
	}

	/**
	 * Sets the value of the accountNumbers property.
	 * 
	 * @param value
	 *            allowed object is {@link AccountNumbers }
	 * 
	 */
	public void setAccountNumbers(AccountNumbers value)
	{
		this.accountNumbers = value;
	}

	/**
	 * Gets the value of the attributes property.
	 * 
	 * @return possible object is {@link ArrayOfAttribute }
	 * 
	 */
	public ArrayOfAttribute getAttributes()
	{
		return attributes;
	}

	/**
	 * Sets the value of the attributes property.
	 * 
	 * @param value
	 *            allowed object is {@link ArrayOfAttribute }
	 * 
	 */
	public void setAttributes(ArrayOfAttribute value)
	{
		this.attributes = value;
	}

}
