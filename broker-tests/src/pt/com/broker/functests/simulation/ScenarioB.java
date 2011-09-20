package pt.com.broker.functests.simulation;

import org.caudexorigo.Shutdown;

import pt.com.broker.client.HostInfo;
import pt.com.broker.functests.simulation.helpers.Consumers;
import pt.com.broker.functests.simulation.helpers.Producers;
import pt.com.broker.functests.simulation.helpers.SyncConsumers;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class ScenarioB
{

	public static void run()
	{
		run(TestMachines.All);
	}

	public static void run(TestMachines testMachines)
	{
		/*
		 * 2 topic producers (connected to different agents) produce 10k messages, sleep for 5 seconds, and produce messages again. Messages expire after 18 seconds 2 virtual queue async consumers (connected to different agents) connected for 30s, disconnect for 10s and connect again 2 virtual queue sync consumers (connected to different agents). Clients sleep for 2 seconds before ack.
		 */

		final String bTopic = "/topic/bbb";

		// Create Sync Consumers
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			SyncConsumers bbbSC1 = new SyncConsumers(30, String.format("sb1@%s", bTopic), 2000, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S B - SC1 ");
			bbbSC1.init();
			bbbSC1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			SyncConsumers bbbSC2 = new SyncConsumers(30, String.format("sb1@%s", bTopic), 2000, 0, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S B - SC2 ");
			bbbSC2.init();
			bbbSC2.start();
		}

		// Create Virtual Queue Consumers
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Consumers bbbVQC1 = new Consumers(DestinationType.VIRTUAL_QUEUE, 1, String.format("sb2@%s", bTopic), 50, 0, 0, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S B - VQ1 ");
			bbbVQC1.init();
			bbbVQC1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Consumers bbbVQC2 = new Consumers(DestinationType.VIRTUAL_QUEUE, 1, String.format("sb2@%s", bTopic), 50, 0, 0, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S B - VQ2 ");
			bbbVQC2.init();
			bbbVQC2.start();
		}

		// Create Topic Producers
		if ((testMachines == TestMachines.Machine1) || (testMachines == TestMachines.All))
		{
			Producers bbbP1 = new Producers(1, bTopic, DestinationType.TOPIC, 0, 100, new HostInfo(MainAll.Agent1Hostname, MainAll.Agent1Port), NetProtocolType.PROTOCOL_BUFFER, "S B - p1 ");
			// bbbP1.setMessageExpiration(18000);
			bbbP1.init();
			bbbP1.start();
		}
		if ((testMachines == TestMachines.Machine2) || (testMachines == TestMachines.All))
		{
			Producers bbbP2 = new Producers(1, bTopic, DestinationType.TOPIC, 0, 100, new HostInfo(MainAll.Agent2Hostname, MainAll.Agent2Port), NetProtocolType.PROTOCOL_BUFFER, "S B - p2 ");
			// bbbP2.setMessageExpiration(18000);
			bbbP2.init();
			bbbP2.start();
		}
	}

	public static void main(String[] args)
	{
		System.out.println("Starting Scenario B");

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
				System.out.println(String.format("Expecting %s [ machine1 | machine2 ] ", "ScenarioB"));
				Shutdown.now();
			}
		}

		run(machines);
	}

}
