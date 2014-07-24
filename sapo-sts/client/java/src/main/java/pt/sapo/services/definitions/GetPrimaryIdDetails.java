
package pt.sapo.services.definitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ESBCredentials" type="{http://services.sapo.pt/definitions}ESBCredentials" minOccurs="0"/>
 *         &lt;element name="UserLogin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="JSON" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="UserType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="UserLoginCredentialsStore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "json",
    "userType",
    "userLoginCredentialsStore"
})
@XmlRootElement(name = "GetPrimaryIdDetails")
public class GetPrimaryIdDetails {

    @XmlElement(name = "ESBCredentials")
    protected ESBCredentials esbCredentials;
    @XmlElement(name = "UserLogin")
    protected String userLogin;
    @XmlElement(name = "JSON")
    protected Boolean json;
    @XmlElement(name = "UserType")
    protected String userType;
    @XmlElement(name = "UserLoginCredentialsStore")
    protected String userLoginCredentialsStore;

    /**
     * Gets the value of the esbCredentials property.
     * 
     * @return
     *     possible object is
     *     {@link ESBCredentials }
     *     
     */
    public ESBCredentials getESBCredentials() {
        return esbCredentials;
    }

    /**
     * Sets the value of the esbCredentials property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESBCredentials }
     *     
     */
    public void setESBCredentials(ESBCredentials value) {
        this.esbCredentials = value;
    }

    /**
     * Gets the value of the userLogin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserLogin() {
        return userLogin;
    }

    /**
     * Sets the value of the userLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserLogin(String value) {
        this.userLogin = value;
    }

    /**
     * Gets the value of the json property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isJSON() {
        return json;
    }

    /**
     * Sets the value of the json property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setJSON(Boolean value) {
        this.json = value;
    }

    /**
     * Gets the value of the userType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserType() {
        return userType;
    }

    /**
     * Sets the value of the userType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserType(String value) {
        this.userType = value;
    }

    /**
     * Gets the value of the userLoginCredentialsStore property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserLoginCredentialsStore() {
        return userLoginCredentialsStore;
    }

    /**
     * Sets the value of the userLoginCredentialsStore property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserLoginCredentialsStore(String value) {
        this.userLoginCredentialsStore = value;
    }

}
