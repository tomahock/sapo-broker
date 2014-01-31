package pt.com.broker.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BrokerInfo
{
	private static String VERSION;

	static
	{
		try
		{
			java.io.InputStream in = BrokerInfo.class.getResourceAsStream("/VERSION.txt");
			InputStreamReader isr = new InputStreamReader(in);
			BufferedReader br = new BufferedReader(isr);

			VERSION = br.readLine().trim();
		}
		catch (Exception e)
		{
			VERSION = "Unable to read version file from JAR.";
		}

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
			e.printStackTrace();
		}
	}
}
