package pt.sapo.services.definitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

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
 *         &lt;element name="ESBCredentials" type="{http://services.sapo.pt/definitions}ESBCredentials"/>
 *         &lt;element name="UserLogin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UserType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Application" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ExpirationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="Services" type="{http://services.sapo.pt/definitions}Services"/>
 *         &lt;element name="ExtraInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OAuthOptions" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
		"esbCredentials",
		"userLogin",
		"userType",
		"application",
		"expirationDate",
		"services",
		"extraInfo",
		"oAuthOptions"
})
@XmlRootElement(name = "GetRevokableToken")
public class GetRevokableToken
{

	@XmlElement(name = "ESBCredentials", required = true)
	protected ESBCredentials esbCredentials;
	@XmlElement(name = "UserLogin")
	protected String userLogin;
	@XmlElement(name = "UserType")
	protected String userType;
	@XmlElement(name = "Application", required = true)
	protected String application;
	@XmlElement(name = "ExpirationDate")
	@XmlSchemaType(name = "dateTime")
	protected XMLGregorianCalendar expirationDate;
	@XmlElement(name = "Services", required = true)
	protected Services services;
	@XmlElement(name = "ExtraInfo")
	protected String extraInfo;
	@XmlElement(name = "OAuthOptions")
	protected String oAuthOptions;

	/**
	 * Gets the value of the esbCredentials property.
	 * 
	 * @return possible object is {@link ESBCredentials }
	 * 
	 */
	public ESBCredentials getESBCredentials()
	{
		return esbCredentials;
	}

	/**
	 * Sets the value of the esbCredentials property.
	 * 
	 * @param value
	 *            allowed object is {@link ESBCredentials }
	 * 
	 */
	public void setESBCredentials(ESBCredentials value)
	{
		this.esbCredentials = value;
	}

	/**
	 * Gets the value of the userLogin property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUserLogin()
	{
		return userLogin;
	}

	/**
	 * Sets the value of the userLogin property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUserLogin(String value)
	{
		this.userLogin = value;
	}

	/**
	 * Gets the value of the userType property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUserType()
	{
		return userType;
	}

	/**
	 * Sets the value of the userType property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUserType(String value)
	{
		this.userType = value;
	}

	/**
	 * Gets the value of the application property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getApplication()
	{
		return application;
	}

	/**
	 * Sets the value of the application property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setApplication(String value)
	{
		this.application = value;
	}

	/**
	 * Gets the value of the expirationDate property.
	 * 
	 * @return possible object is {@link javax.xml.datatype.XMLGregorianCalendar }
	 * 
	 */
	public XMLGregorianCalendar getExpirationDate()
	{
		return expirationDate;
	}

	/**
	 * Sets the value of the expirationDate property.
	 * 
	 * @param value
	 *            allowed object is {@link javax.xml.datatype.XMLGregorianCalendar }
	 * 
	 */
	public void setExpirationDate(XMLGregorianCalendar value)
	{
		this.expirationDate = value;
	}

	/**
	 * Gets the value of the services property.
	 * 
	 * @return possible object is {@link Services }
	 * 
	 */
	public Services getServices()
	{
		return services;
	}

	/**
	 * Sets the value of the services property.
	 * 
	 * @param value
	 *            allowed object is {@link Services }
	 * 
	 */
	public void setServices(Services value)
	{
		this.services = value;
	}

	/**
	 * Gets the value of the extraInfo property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getExtraInfo()
	{
		return extraInfo;
	}

	/**
	 * Sets the value of the extraInfo property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setExtraInfo(String value)
	{
		this.extraInfo = value;
	}

	/**
	 * Gets the value of the oAuthOptions property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getOAuthOptions()
	{
		return oAuthOptions;
	}

	/**
	 * Sets the value of the oAuthOptions property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setOAuthOptions(String value)
	{
		this.oAuthOptions = value;
	}

}
