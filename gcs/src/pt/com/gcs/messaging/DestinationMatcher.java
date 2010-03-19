package pt.com.gcs.messaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DestinationMatcher uses regular expressions to determine if a given subscription matches a given topic name.
 * 
 */

public class DestinationMatcher
{
	private static Logger log = LoggerFactory.getLogger(DestinationMatcher.class);

	public static boolean match(String subscriptionName, String topicName)
	{
		try
		{
			Pattern p = PatternCache.get(subscriptionName);
			Matcher m = p.matcher(topicName);
			return m.matches();
		}
		catch (Throwable t)
		{
			String message = String.format("match-> subscriptionName: '%s'; topicName: '%s'", subscriptionName, topicName);
			log.error(message, t);
			return false;
		}
	}
}
