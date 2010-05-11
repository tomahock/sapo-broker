package pt.com.broker.monitorization.collector;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentName
{
	private static final Logger log = LoggerFactory.getLogger(AgentName.class);

	public static String FAIL = "Failed to get hostname";

	private static Map<String, String> hostnames = new HashMap<String, String>();

	protected static String getHostname(String agentName)
	{
		String hostname = null;
		synchronized (hostnames)
		{
			hostname = hostnames.get(agentName);
		}
		if (hostname == null)
		{
			hostname = findHostname(agentName);
			synchronized (hostnames)
			{
				hostname = hostnames.put(agentName, hostname);
			}
		}

		return hostname;
	}

	public static String findHostname(String agentName)
	{
		String ip = StringUtils.substringBefore(agentName, ":");

		InetAddress[] addresses = null;
		try
		{
			addresses = InetAddress.getAllByName(ip);
		}
		catch (UnknownHostException e)
		{
			log.error("Failed to get host name", e);
			return FAIL;
		}
		if ((addresses != null) && (addresses.length > 0))
		{
			String hostname = addresses[0].getHostName();
			return hostname;
		}
		return null;
	}

	public static void main(String[] args)
	{
		String result = AgentName.findHostname("10.135.33.23:3315");
		System.out.println(result);
	}
}
