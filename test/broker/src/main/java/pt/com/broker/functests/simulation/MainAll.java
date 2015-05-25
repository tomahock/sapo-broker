package pt.com.broker.functests.simulation;

import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.functests.conf.ConfigurationInfo;

public class MainAll
{
	private static final Logger log = LoggerFactory.getLogger(MainAll.class);

	public static final String Agent1Hostname;
	public static final int Agent1Port;
	public static final String Agent2Hostname;
	public static final int Agent2Port;

	static
	{
		ConfigurationInfo.init();

		Agent1Hostname = ConfigurationInfo.getParameter("agent1-host");
		Agent2Hostname = ConfigurationInfo.getParameter("agent2-host");

		Agent1Port = Integer.parseInt(ConfigurationInfo.getParameter("agent1-port"));
		Agent2Port = Integer.parseInt(ConfigurationInfo.getParameter("agent2-port"));
	}

	public static void main(String[] args)
	{
		/***** Scenario A *****/

		ScenarioA.run();
		Sleep.time(1000);

		/***** Scenario B *****/

		ScenarioB.run();
		Sleep.time(1000);

		/***** Scenario C *****/

		ScenarioC.run();
		Sleep.time(1000);

		/***** Scenario D *****/

		ScenarioD.run();
		Sleep.time(1000);

		/***** Scenario E *****/

		ScenarioE.run();

		log.info("Tests running!!");

	}

}
