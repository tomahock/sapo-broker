
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
 *         &lt;element name="GetPrimaryIdResult" type="{http://services.sapo.pt/definitions}UserInfo" minOccurs="0"/>
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
    "getPrimaryIdResult"
})
@XmlRootElement(name = "GetPrimaryIdResponse")
public class GetPrimaryIdResponse {

    @XmlElement(name = "GetPrimaryIdResult")
    protected UserInfo getPrimaryIdResult;

    /**
     * Gets the value of the getPrimaryIdResult property.
     * 
     * @return
     *     possible object is
     *     {@link UserInfo }
     *     
     */
    public UserInfo getGetPrimaryIdResult() {
        return getPrimaryIdResult;
    }

    /**
     * Sets the value of the getPrimaryIdResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserInfo }
     *     
     */
    public void setGetPrimaryIdResult(UserInfo value) {
        this.getPrimaryIdResult = value;
    }

}
