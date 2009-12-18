package pt.com.broker.monitorization.consolidator.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentName
{
	private static final Logger log = LoggerFactory.getLogger(AgentName.class);
	
	private static String FAILED_TO_GET_HOSTNAME = "Failed to get hostname";
	
	private static Map<String, String> hostnames = new HashMap<String, String>();

	public static String getHostname(String agentName)
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
			int idx = agentName.indexOf(':');

			String ip = agentName.substring(0, idx);

			InetAddress addr = null;
			try
			{
				addr = InetAddress.getByName(ip);
			}
			catch (UnknownHostException e)
			{
				log.error("Failed to get host name", e);
				return FAILED_TO_GET_HOSTNAME;
			}
			return addr.getHostName();


	}

}
