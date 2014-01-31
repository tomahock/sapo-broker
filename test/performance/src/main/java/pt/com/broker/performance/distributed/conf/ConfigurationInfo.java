package pt.com.broker.performance.distributed.conf;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.caudexorigo.Shutdown;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.performance.distributed.conf.Agents.Agent;
import pt.com.broker.types.NetProtocolType;

public class ConfigurationInfo
{
	private static final Logger log = LoggerFactory.getLogger(ConfigurationInfo.class);

	private static TestConfiguration configuration;

	public static class AgentInfo
	{

		public final String id;
		public final String hostname;
		public final int tcpPort;
		public final int httpPort;

		AgentInfo(String id, String hostname, int tcpPort, int httpPort)
		{
			this.id = id;
			this.hostname = hostname;
			this.tcpPort = tcpPort;
			this.httpPort = httpPort;
		}
	}

	private static HashMap<String, AgentInfo> agents = new HashMap<String, AgentInfo>();

	private static AgentInfo defaultAgent;

	static
	{
		JAXBContext jc;
		Unmarshaller u = null;
		try
		{
			jc = JAXBContext.newInstance("pt.com.broker.performance.distributed.conf");
			u = jc.createUnmarshaller();
			String filename = System.getProperty("perf-test-configuration");

			if (filename == null)
			{
				log.error("Property 'perf-test-configuration' not defined. Exiting...");
				Shutdown.now();
			}
			File f = new File(filename);
			boolean b = f.exists();
			if (!b)
			{
				log.error("Configuration file (" + filename + ") was not found.");
			}
			configuration = (TestConfiguration) u.unmarshal(f);
		}
		catch (Throwable e)
		{
			log.error("Configuration initialization failed.", e);
		}
	}

	private static void loadAgents()
	{
		if (getConfiguration() == null)
		{
			org.caudexorigo.Shutdown.now();
		}

		Agents agents = getConfiguration().getAgents();

		List<Agent> agentList = agents.getAgent();

		for (Agent agent : agentList)
		{
			ConfigurationInfo.getAgents().put(agent.getAgentId(), new AgentInfo(agent.getAgentId(), agent.getHostname(), agent.getTcpPort().intValue(), agent.getHttpPort().intValue()));
		}

		defaultAgent = ConfigurationInfo.getAgents().get(agents.getDefaultAgent());
	}

	public static void init()
	{
		loadAgents();
	}

	public static HashMap<String, AgentInfo> getAgents()
	{
		return agents;
	}

	public static TestConfiguration getConfiguration()
	{
		return configuration;
	}

	public static AgentInfo getDefaultAgent()
	{
		return defaultAgent;
	}

	public static NetProtocolType getEncoding()
	{
		String encoding = configuration.getTests().getEncoding();
		return NetProtocolType.valueOf(encoding);
	}

}
