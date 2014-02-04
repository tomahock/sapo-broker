package pt.com.broker.auth.saposts;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Utility class for SAPO STS XML namespac.
 * 
 */

public class SapoSTSNamespaceContext implements NamespaceContext
{
	private static SapoSTSNamespaceContext instance = new SapoSTSNamespaceContext();

	public static SapoSTSNamespaceContext getInstance()
	{
		return instance;
	}

	public String getNamespaceURI(String prefix)
	{
		if (prefix.equals("tns"))
			return "http://services.sapo.pt/exceptions";
		else
			return XMLConstants.NULL_NS_URI;
	}

	public String getPrefix(String namespace)
	{
		if (namespace.equals("http://services.sapo.pt/exceptions"))
			return "tns";
		else
			return null;
	}

	public Iterator getPrefixes(String namespace)
	{
		return null;
	}
}