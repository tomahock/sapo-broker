
package pt.sapo.services.definitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ESBCredentials complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ESBCredentials">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ESBUsername" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBPassword" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBRoles" type="{http://services.sapo.pt/definitions}ESBRoles" minOccurs="0"/>
 *         &lt;element name="ESBTokenTimeToLive" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBTokenExtraInfo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBPrimaryId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBUserType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBCredentialsStore" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBClientAppId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBScope" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ESBIdToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ESBCredentials", propOrder = {
    "esbUsername",
    "esbPassword",
    "esbToken",
    "esbRoles",
    "esbTokenTimeToLive",
    "esbTokenExtraInfo",
    "esbPrimaryId",
    "esbUserType",
    "esbCredentialsStore",
    "esbClientAppId",
    "esbScope",
    "esbIdToken"
})
public class ESBCredentials {

    @XmlElement(name = "ESBUsername")
    protected String esbUsername;
    @XmlElement(name = "ESBPassword")
    protected String esbPassword;
    @XmlElement(name = "ESBToken")
    protected String esbToken;
    @XmlElement(name = "ESBRoles")
    protected ESBRoles esbRoles;
    @XmlElement(name = "ESBTokenTimeToLive")
    protected String esbTokenTimeToLive;
    @XmlElement(name = "ESBTokenExtraInfo")
    protected String esbTokenExtraInfo;
    @XmlElement(name = "ESBPrimaryId")
    protected String esbPrimaryId;
    @XmlElement(name = "ESBUserType")
    protected String esbUserType;
    @XmlElement(name = "ESBCredentialsStore")
    protected String esbCredentialsStore;
    @XmlElement(name = "ESBClientAppId")
    protected String esbClientAppId;
    @XmlElement(name = "ESBScope")
    protected String esbScope;
    @XmlElement(name = "ESBIdToken")
    protected String esbIdToken;

    /**
     * Gets the value of the esbUsername property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBUsername() {
        return esbUsername;
    }

    /**
     * Sets the value of the esbUsername property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBUsername(String value) {
        this.esbUsername = value;
    }

    /**
     * Gets the value of the esbPassword property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBPassword() {
        return esbPassword;
    }

    /**
     * Sets the value of the esbPassword property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBPassword(String value) {
        this.esbPassword = value;
    }

    /**
     * Gets the value of the esbToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBToken() {
        return esbToken;
    }

    /**
     * Sets the value of the esbToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBToken(String value) {
        this.esbToken = value;
    }

    /**
     * Gets the value of the esbRoles property.
     * 
     * @return
     *     possible object is
     *     {@link ESBRoles }
     *     
     */
    public ESBRoles getESBRoles() {
        return esbRoles;
    }

    /**
     * Sets the value of the esbRoles property.
     * 
     * @param value
     *     allowed object is
     *     {@link ESBRoles }
     *     
     */
    public void setESBRoles(ESBRoles value) {
        this.esbRoles = value;
    }

    /**
     * Gets the value of the esbTokenTimeToLive property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBTokenTimeToLive() {
        return esbTokenTimeToLive;
    }

    /**
     * Sets the value of the esbTokenTimeToLive property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBTokenTimeToLive(String value) {
        this.esbTokenTimeToLive = value;
    }

    /**
     * Gets the value of the esbTokenExtraInfo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBTokenExtraInfo() {
        return esbTokenExtraInfo;
    }

    /**
     * Sets the value of the esbTokenExtraInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBTokenExtraInfo(String value) {
        this.esbTokenExtraInfo = value;
    }

    /**
     * Gets the value of the esbPrimaryId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBPrimaryId() {
        return esbPrimaryId;
    }

    /**
     * Sets the value of the esbPrimaryId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBPrimaryId(String value) {
        this.esbPrimaryId = value;
    }

    /**
     * Gets the value of the esbUserType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBUserType() {
        return esbUserType;
    }

    /**
     * Sets the value of the esbUserType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBUserType(String value) {
        this.esbUserType = value;
    }

    /**
     * Gets the value of the esbCredentialsStore property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBCredentialsStore() {
        return esbCredentialsStore;
    }

    /**
     * Sets the value of the esbCredentialsStore property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBCredentialsStore(String value) {
        this.esbCredentialsStore = value;
    }

    /**
     * Gets the value of the esbClientAppId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBClientAppId() {
        return esbClientAppId;
    }

    /**
     * Sets the value of the esbClientAppId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBClientAppId(String value) {
        this.esbClientAppId = value;
    }

    /**
     * Gets the value of the esbScope property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBScope() {
        return esbScope;
    }

    /**
     * Sets the value of the esbScope property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBScope(String value) {
        this.esbScope = value;
    }

    /**
     * Gets the value of the esbIdToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getESBIdToken() {
        return esbIdToken;
    }

    /**
     * Sets the value of the esbIdToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setESBIdToken(String value) {
        this.esbIdToken = value;
    }

}
