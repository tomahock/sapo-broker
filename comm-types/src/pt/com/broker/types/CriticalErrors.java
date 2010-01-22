package pt.com.broker.types;

import org.caudexorigo.ErrorAnalyser;
import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CriticalErrors
{
	private static final Logger log = LoggerFactory.getLogger(CriticalErrors.class);

	private static final String ERROR_MESSAGE = "Too many open files".toLowerCase();

	private static void exitIfUlimit(Throwable t)
	{
		if (t.getMessage() != null)
		{
			String emsg = t.getMessage().toLowerCase();
			if (emsg.contains(ERROR_MESSAGE))
			{
				log.info("Shutting down");
				Shutdown.now();
			}
		}
	}

	public static void exitIfCritical(Throwable cause)
	{
		ErrorAnalyser.exitIfOOM(cause);
		exitIfUlimit(cause);
	}
}
