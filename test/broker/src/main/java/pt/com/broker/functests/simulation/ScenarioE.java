package pt.com.broker.functests.simulation;

import org.caudexorigo.Shutdown;

import pt.com.broker.client.HostInfo;
import pt.com.broker.functests.simulation.helpers.Consumers;
import pt.com.broker.functests.simulation.helpers.Producers;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class ScenarioE
{
	public static void run()
	{
		run(TestMachines.All);
	}

	public static void run(TestMachines testMachines)
	{
		/***** Scenario E *****/

		/*
		 * 2 queue producers (connected to different agents) produce 1k messages, sleep for 5 seconds, and produce messages again. 1 async consumers.
		 */

		final String eeeQueue = "/queue/ddd";

		// Create Queue Producer
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Producers eeeP1 = new Producers(2, eeeQueue, DestinationType.QUEUE, 0, 200, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S E - p1 ");
			eeeP1.init();
			eeeP1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Producers eeeP2 = new Producers(2, eeeQueue, DestinationType.QUEUE, 0, 200, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S E - p1 ");
			eeeP2.init();
			eeeP2.start();
		}

		// Create Queue Consumers
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Consumers eeeVQC1 = new Consumers(DestinationType.QUEUE, 1, String.format("%s", eeeQueue), 0, 0, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "C E - Q 1 ");
			eeeVQC1.init();
			eeeVQC1.start();
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Starting Scenario E");

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
				System.out.println(String.format("Expecting %s [ machine1 | machine2 ] ", "ScenarioE"));
				Shutdown.now();
			}
		}

		run(machines);
	}

}
