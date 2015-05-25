@XmlSchema(
		elementFormDefault = XmlNsForm.QUALIFIED,
		namespace = "http://www.w3.org/2003/05/soap-envelope",
		attributeFormDefault = XmlNsForm.QUALIFIED,
		xmlns = {
				@XmlNs(prefix = "mq", namespaceURI = "http://services.sapo.pt/broker"),
				@XmlNs(prefix = "soap", namespaceURI = "http://www.w3.org/2003/05/soap-envelope"),
				@XmlNs(prefix = "wsa", namespaceURI = "http://www.w3.org/2005/08/addressing")
		})
package pt.com.broker.codec.xml.soap;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

