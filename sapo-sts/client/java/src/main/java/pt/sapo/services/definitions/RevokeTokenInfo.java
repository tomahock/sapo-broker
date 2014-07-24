
package pt.sapo.services.definitions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RevokeTokenInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RevokeTokenInfo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RevokeActionSuccess" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RevokeTokenInfo", propOrder = {
    "revokeActionSuccess"
})
public class RevokeTokenInfo {

    @XmlElement(name = "RevokeActionSuccess")
    protected boolean revokeActionSuccess;

    /**
     * Gets the value of the revokeActionSuccess property.
     * 
     */
    public boolean isRevokeActionSuccess() {
        return revokeActionSuccess;
    }

    /**
     * Sets the value of the revokeActionSuccess property.
     * 
     */
    public void setRevokeActionSuccess(boolean value) {
        this.revokeActionSuccess = value;
    }

}
