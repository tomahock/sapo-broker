package pt.com.broker.functests.simulation;

import org.caudexorigo.Shutdown;

import pt.com.broker.client.HostInfo;
import pt.com.broker.functests.simulation.helpers.Consumers;
import pt.com.broker.functests.simulation.helpers.Producers;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class ScenarioC
{
	public static void run()
	{
		run(TestMachines.All);
	}

	public static void run(TestMachines testMachines)
	{
		/*
		 * 5 topic producers + 10 topic producers (connected to different agents) produce 100msg/s, sleep for 5 seconds, and produce messages again 2 async consumers (connected to different agents).
		 */

		final String cTopic = "/topic/ccc";

		// Create Topic Producer
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Producers cccP1 = new Producers(1/* 5 */, cTopic, DestinationType.TOPIC, 50, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S C - p1 ");
			cccP1.init();
			cccP1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Producers cccP2 = new Producers(2/* 10 */, cTopic, DestinationType.TOPIC, 50, 0, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S C - p2 ");
			cccP2.init();
			cccP2.start();
		}
		// Create Virtual Queue Consumer
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Consumers cccVQC1 = new Consumers(DestinationType.TOPIC, 1, String.format("%s", cTopic), 0, 0, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S C - c1 ");
			cccVQC1.init();
			cccVQC1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Consumers cccVQC2 = new Consumers(DestinationType.TOPIC, 1, String.format("%s", cTopic), 0, 0, 0, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S C - c2 ");
			cccVQC2.init();
			cccVQC2.start();
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Starting Scenario C");

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
				System.out.println(String.format("Expecting %s [ machine1 | machine2 ] ", "ScenarioC"));
				Shutdown.now();
			}
		}

		run(machines);
	}

}
