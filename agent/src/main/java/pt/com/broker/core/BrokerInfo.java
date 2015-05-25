package pt.com.broker.core;

public class BrokerInfo
{
	private final static String VERSION;

	static
	{
		VERSION = System.getProperty("project-version");
	}

	public static final String getVersion()
	{
		return VERSION;
	}

	public static void main(String[] args)
	{
		try
		{
			System.out.println("VERSION: " + VERSION);
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
	}
}
