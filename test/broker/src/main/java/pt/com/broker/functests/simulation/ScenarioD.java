package pt.com.broker.functests.simulation;

import org.caudexorigo.Shutdown;

import pt.com.broker.client.HostInfo;
import pt.com.broker.functests.simulation.helpers.Consumers;
import pt.com.broker.functests.simulation.helpers.Producers;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class ScenarioD
{

	public static void run()
	{
		run(TestMachines.All);
	}

	public static void run(TestMachines testMachines)
	{
		/*
		 * 4 queue producers (connected to different agents) producers 100msg/s, sleep for 5 seconds, and produce messages again 2 async consumers (connected to different agents).
		 */

		final String dddQueue = "/queue/ddd";

		// Create Queue Producer
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Producers dddP1 = new Producers(2, dddQueue, DestinationType.QUEUE, 50, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S D - p1 ");
			dddP1.init();
			dddP1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Producers dddP2 = new Producers(2, dddQueue, DestinationType.QUEUE, 50, 0, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S D - p2 ");
			dddP2.init();
			dddP2.start();
		}

		// Create Queue Consumers
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Consumers dddVQC1 = new Consumers(DestinationType.QUEUE, 1, String.format("%s", dddQueue), 0, 0, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S D - c1 ");
			dddVQC1.init();
			dddVQC1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Consumers dddVQC2 = new Consumers(DestinationType.QUEUE, 1, String.format("%s", dddQueue), 0, 0, 0, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S D - c2 ");
			dddVQC2.init();
			dddVQC2.start();
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Starting Scenario D");

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
				System.out.println(String.format("Expecting %s [ machine1 | machine2 ] ", "ScenarioD"));
				Shutdown.now();
			}
		}

		run(machines);
	}
}
