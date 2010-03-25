package pt.com.broker.functests.simulation;

import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainAll
{
	private static final Logger log = LoggerFactory.getLogger(MainAll.class);
	
	public static final String Agent1Hostname = "localhost";
	public static final int Agent1Port = 3323;
	public static final String Agent2Hostname = "localhost";
	public static final int Agent2Port = 3423;
	
	public static void main(String[] args)
	{
		System.out.println("TODO: Agens are hardcoded...");
		
		
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

