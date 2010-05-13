package pt.com.broker.monitorization;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.caudexorigo.ds.Cache;
import org.caudexorigo.ds.CacheFiller;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentHostname
{
	private static Logger log = LoggerFactory.getLogger(AgentHostname.class);
	public static String FAIL = "Failed to get hostname";

	private static Cache<String, String> hostnames = new Cache<String, String>();
	private static CacheFiller<String, String> CF_HOSTS = new CacheFiller<String, String>()
	{
		@Override
		public String populate(String agentName)
		{
			return findHostname(agentName);
		}
	};

	public static String get(String agentName)
	{
		String hostname = null;
		if (StringUtils.isBlank(agentName))
		{
			return null;
		}
		try
		{
			hostname = hostnames.get(agentName, CF_HOSTS);
		}
		catch (InterruptedException e)
		{
			log.error("Failed to get hostname");
		}
		return hostname;
	}

	public static String findHostname(String agentName)
	{
		String ip = StringUtils.substringBefore(agentName, ":");

		log.info("Reverse DNS lookup for: {}", ip);

		InetAddress[] addresses = null;
		try
		{
			addresses = InetAddress.getAllByName(ip);
		}
		catch (UnknownHostException e)
		{
			log.error("Failed to get host name", e);
		}
		if ((addresses != null) && (addresses.length > 0))
		{
			String hostname = addresses[0].getHostName();
			return hostname;
		}
		return agentName;
	}
}
