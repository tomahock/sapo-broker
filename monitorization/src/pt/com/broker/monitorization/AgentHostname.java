package pt.com.broker.monitorization;

import org.caudexorigo.ds.Cache;
import org.caudexorigo.ds.CacheFiller;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.monitorization.collector.AgentName;

public class AgentHostname
{
	private static Logger log = LoggerFactory.getLogger(AgentHostname.class);

	private static Cache<String, String> hostnames = new Cache<String, String>();
	private static CacheFiller<String, String> CF_HOSTS = new CacheFiller<String, String>()
	{
		@Override
		public String populate(String agentName)
		{
			String hostname = AgentName.findHostname(agentName);
			return ((agentName == null) || (agentName.equals(AgentName.FAIL))) ? agentName : hostname;
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
}
