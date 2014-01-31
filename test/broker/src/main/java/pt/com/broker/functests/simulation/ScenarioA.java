package pt.com.broker.functests.simulation;

import org.caudexorigo.Shutdown;

import pt.com.broker.client.HostInfo;
import pt.com.broker.functests.simulation.helpers.Consumers;
import pt.com.broker.functests.simulation.helpers.Producers;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class ScenarioA
{

	public static void run()
	{
		run(TestMachines.All);
	}

	public static void run(TestMachines testMachines)
	{
		/***** Scenario A *****/
		/*
		 * 2 topic producers (connected to different agents) produce 300msg/s each 2 virtual queue assync consumers (connected to different agents) connected for 30s, disconnect for 10s and connect again
		 */

		final String aTopic = "/topic/aaa";

		// Create Virtual Queue Consumers

		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Consumers aaaVQC1 = new Consumers(DestinationType.VIRTUAL_QUEUE, 1, String.format("sa@%s", aTopic), 0, 30000, 10000, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S A - vq1 ");
			aaaVQC1.init();
			aaaVQC1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Consumers aaaVQC2 = new Consumers(DestinationType.VIRTUAL_QUEUE, 1, String.format("sa@%s", aTopic), 0, 30000, 10000, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S A - vq2 ");
			aaaVQC2.init();
			aaaVQC2.start();
		}

		// Create Producers
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Producers aaaP1 = new Producers(1, aTopic, DestinationType.TOPIC, 30, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S A - p1 ");
			aaaP1.init();
			aaaP1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Producers aaaP2 = new Producers(1, aTopic, DestinationType.TOPIC, 30, 0, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S A - p1 ");
			aaaP2.init();
			aaaP2.start();
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Starting Scenario A");

		TestMachines machines = TestMachines.All;

		if (args.length == 1)
		{
			if (args[0].equals("machine1"))
			{
				machines = TestMachines.Machine1;
				System.out.println("Working on machine1");
			}
			else if (args[0].equals("machine2"))
			{
				machines = TestMachines.Machine2;
				System.out.println("Working on machine2");
			}
			else
			{
				System.out.println(String.format("Expecting %s [ machine1 | machine2 ] ", "ScenarioA"));
				Shutdown.now();
			}
		}

		run(machines);
	}

}
