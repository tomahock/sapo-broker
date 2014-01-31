package pt.com.broker.auth;

import org.w3c.dom.Element;

/**
 * ProviderInfo represents some service provider, such as Authentication Credentials Validator, that is dynamically loaded.
 * 
 */

public class ProviderInfo
{
	private String name;
	private String className;
	private Element parameters;

	public ProviderInfo(String name, String className, Element parameters)
	{
		this.name = name;
		this.className = className;
		this.parameters = parameters;
	}

	public String getName()
	{
		return name;
	}

	public String getClassName()
	{
		return className;
	}

	/**
	 * 
	 * @return An XML element with provider specific information.
	 */
	public Element getParameters()
	{
		return parameters;
	}

}
