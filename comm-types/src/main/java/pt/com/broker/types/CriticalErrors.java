package pt.com.broker.types;

import org.caudexorigo.Shutdown;

public class CriticalErrors
{
	private static final String ERROR_MESSAGE = "Too many open files".toLowerCase();

	private static void exitIfUlimit(Throwable t)
	{
		if (t.getMessage() != null)
		{
			String emsg = t.getMessage().toLowerCase();
			if (emsg.contains(ERROR_MESSAGE))
			{
				Shutdown.now(t);
			}
		}
	}

	public static void exitIfCritical(Throwable cause)
	{
		// ErrorAnalyser.exitIfOOM(cause);
		exitIfUlimit(cause);
	}
}
