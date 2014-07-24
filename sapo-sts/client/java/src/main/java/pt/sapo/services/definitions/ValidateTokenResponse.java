
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
 *         &lt;element name="ValidateTokenResult" type="{http://services.sapo.pt/definitions}TokenInfo"/>
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
    "validateTokenResult"
})
@XmlRootElement(name = "ValidateTokenResponse")
public class ValidateTokenResponse {

    @XmlElement(name = "ValidateTokenResult", required = true)
    protected TokenInfo validateTokenResult;

    /**
     * Gets the value of the validateTokenResult property.
     * 
     * @return
     *     possible object is
     *     {@link TokenInfo }
     *     
     */
    public TokenInfo getValidateTokenResult() {
        return validateTokenResult;
    }

    /**
     * Sets the value of the validateTokenResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link TokenInfo }
     *     
     */
    public void setValidateTokenResult(TokenInfo value) {
        this.validateTokenResult = value;
    }

}
