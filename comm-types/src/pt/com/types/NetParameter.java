package pt.com.types;

public final class NetParameter
{
	private String name;
	private String value;

	public NetParameter(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}
}
