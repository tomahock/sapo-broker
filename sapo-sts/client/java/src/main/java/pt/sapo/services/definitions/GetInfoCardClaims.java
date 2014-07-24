
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
 *         &lt;element name="InfoCardToken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "infoCardToken"
})
@XmlRootElement(name = "GetInfoCardClaims")
public class GetInfoCardClaims {

    @XmlElement(name = "ESBCredentials")
    protected ESBCredentials esbCredentials;
    @XmlElement(name = "InfoCardToken")
    protected String infoCardToken;

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
     * Gets the value of the infoCardToken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfoCardToken() {
        return infoCardToken;
    }

    /**
     * Sets the value of the infoCardToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfoCardToken(String value) {
        this.infoCardToken = value;
    }

}
