
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
 *         &lt;element name="GetRevokableTokenResult" type="{http://services.sapo.pt/definitions}RevokableToken"/>
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
    "getRevokableTokenResult"
})
@XmlRootElement(name = "GetRevokableTokenResponse")
public class GetRevokableTokenResponse {

    @XmlElement(name = "GetRevokableTokenResult", required = true)
    protected RevokableToken getRevokableTokenResult;

    /**
     * Gets the value of the getRevokableTokenResult property.
     * 
     * @return
     *     possible object is
     *     {@link RevokableToken }
     *     
     */
    public RevokableToken getGetRevokableTokenResult() {
        return getRevokableTokenResult;
    }

    /**
     * Sets the value of the getRevokableTokenResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link RevokableToken }
     *     
     */
    public void setGetRevokableTokenResult(RevokableToken value) {
        this.getRevokableTokenResult = value;
    }

}
