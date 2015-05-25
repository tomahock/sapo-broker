package pt.com.broker.codec.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Unsubscribe")
public class Unsubscribe
{
	@XmlAttribute(name = "action-id")
	public String actionId;

	@XmlElement(name = "DestinationName")
	public String destinationName;

	@XmlElement(name = "DestinationType")
	public String destinationType;

	public Unsubscribe()
	{
		destinationName = "";
		destinationType = "";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationName == null) ? 0 : destinationName.hashCode());
		result = prime * result + ((destinationType == null) ? 0 : destinationType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Unsubscribe other = (Unsubscribe) obj;
		if (destinationName == null)
		{
			if (other.destinationName != null)
				return false;
		}
		else if (!destinationName.equals(other.destinationName))
			return false;
		if (destinationType == null)
		{
			if (other.destinationType != null)
				return false;
		}
		else if (!destinationType.equals(other.destinationType))
			return false;
		return true;
	}

}
