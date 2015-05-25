package pt.com.broker.functests;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.functests.conf.ConfigurationInfo;

/**
 * Copyright (c) 2014, SAPO All rights reserved.
 *
 * <p/>
 * Created by Luis Santos<luis.santos@telecom.pt> on 08-07-2014.
 */
public class BaseTest
{

	static final Logger log = LoggerFactory.getLogger(BaseTest.class);

	public static final String AGENT_LAUNCH_SYSTEM_PROPERTY = "agent-launch";
	public static final String LAUNCH_N_AGENTS_SYSTEM_PROPERTY = "n-agents";

	protected static boolean runOnce = true;
	protected static boolean lauchAgent = false;
	protected static int nAgents = 1;

	@BeforeClass()
	public static void loadConfig()
	{
		loadSystemProperties();
		if (runOnce)
		{
			ConfigurationInfo.init();
			Test.setDefaultimeout(10000);
			runOnce = false;
		}
		// brokerAgentsLaucher();
	}

	private static final void loadSystemProperties()
	{
		log.info("Loading system properties.");
		try
		{
			lauchAgent = Boolean.valueOf(System.getProperty(AGENT_LAUNCH_SYSTEM_PROPERTY));
			log.debug("Launch agent system property: {}", lauchAgent);
			nAgents = Integer.valueOf(System.getProperty(LAUNCH_N_AGENTS_SYSTEM_PROPERTY));
			log.debug("Launch n agents: {}", nAgents);
		}
		catch (Exception e)
		{
			log.error("Error loading system properties.", e);
		}
	}

	/*
	 * private static final void brokerAgentsLaucher(){ if(lauchAgent){ for(int i = 0; i < nAgents; i++){ String agentConfigPath = System.getProperty(String.format("%s-%s", Start.AGENT_CONFIG_PATH_PROPERTY, i)); String globalConfigPath = System.getProperty(String.format("%s-%s", Start.GLOBAL_CONFIG_PATH_PROPERTY, i)); log.debug("Launching agent with configuration files: {}; {}", agentConfigPath, globalConfigPath); System.setProperty(Start.AGENT_CONFIG_PATH_PROPERTY, agentConfigPath); System.setProperty(Start.GLOBAL_CONFIG_PATH_PROPERTY, globalConfigPath); log.debug("Agent config path in system property: {}", System.getProperty(Start.AGENT_CONFIG_PATH_PROPERTY)); log.debug("Agent global config path in system property: {}", System.getProperty(Start.GLOBAL_CONFIG_PATH_PROPERTY)); try { int
	 * retVal = JavaProcess.exec(Start.class); log.debug("Returned value for launching Agent: {}", retVal); } catch (IOException | InterruptedException e) { // TODO Auto-generated catch block e.printStackTrace(); } } } }
	 */

}
