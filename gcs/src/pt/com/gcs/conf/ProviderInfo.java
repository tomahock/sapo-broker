package pt.com.gcs.conf;

import org.w3c.dom.Element;

public class ProviderInfo
{
	private String name;
	private String className;
	private Element parameters;
	
	public ProviderInfo(String name, String className, Element parameters){
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
	public Element getParameters()
	{
		return parameters;
	}
	
}
