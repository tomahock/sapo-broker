//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.04.10 at 05:51:46 PM WEST 
//

package pt.com.broker.performance.distributed.conf;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for Consumers complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Consumers">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="consumer" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="agent-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="count" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Consumers", propOrder = { "consumer" })
public class Consumers
{

	protected List<Consumers.Consumer> consumer;
	@XmlAttribute(required = true)
	protected BigDecimal count;

	/**
	 * Gets the value of the consumer property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the consumer property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getConsumer().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Consumers.Consumer }
	 * 
	 * 
	 */
	public List<Consumers.Consumer> getConsumer()
	{
		if (consumer == null)
		{
			consumer = new ArrayList<Consumers.Consumer>();
		}
		return this.consumer;
	}

	/**
	 * Gets the value of the count property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getCount()
	{
		return count;
	}

	/**
	 * Sets the value of the count property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setCount(BigDecimal value)
	{
		this.count = value;
	}

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
	 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *       &lt;attribute name="agent-id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "")
	public static class Consumer
	{

		@XmlAttribute(required = true)
		protected String name;
		@XmlAttribute(name = "agent-id", required = true)
		protected String agentId;

		/**
		 * Gets the value of the name property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Sets the value of the name property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setName(String value)
		{
			this.name = value;
		}

		/**
		 * Gets the value of the agentId property.
		 * 
		 * @return possible object is {@link String }
		 * 
		 */
		public String getAgentId()
		{
			return agentId;
		}

		/**
		 * Sets the value of the agentId property.
		 * 
		 * @param value
		 *            allowed object is {@link String }
		 * 
		 */
		public void setAgentId(String value)
		{
			this.agentId = value;
		}

	}

}
