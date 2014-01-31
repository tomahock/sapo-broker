package pt.com.broker.performance;

import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class Main
{
	private static final DestinationType[] destinationTypes = new DestinationType[] { /* DestinationType.TOPIC/, */DestinationType.QUEUE };
	private static final NetProtocolType[] protocolTypes = new NetProtocolType[] { /* NetProtocolType.SOAP, */NetProtocolType.PROTOCOL_BUFFER,/* NetProtocolType.THRIFT */};
	private static final int[] messageSizes = new int[] { 10, /* 512, 1000, 10 * 1000, 100 * 1000, */200 * 1000 };
	// private static final int producers[] = new int[] { 1, 1, 2, 2, 4, 4, 4, 6, 6, 6, 8, 8, 8, 1, 1, 2, 2, 4, 4, 4, 6, 6, 6, 8, 8, 8 };
	// private static final int localConsumers[] = new int[] { 1, 2, 1, 2, 1, 2, 4, 1, 2, 6, 1, 2, 8, 1, 2, 1, 2, 1, 2, 4, 1, 2, 6, 1, 2, 8 };
	// private static final int remoteConsumers[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };

	// private static final int clients[][] = new int[][] { new int[] { 1, 1, 0 }, new int[] { 4, 1, 0 }, new int[] { 4, 4, 0 } }; // int nrProducers, int nrLocalConsumers, int nrRemoteConsumers
	private static final int clients[][] = new int[][] { new int[] { 1, 0, 0 }, new int[] { 4, 0, 0 } }; // int nrProducers, int nrLocalConsumers, int nrRemoteConsumers

	public static void main(String[] args)
	{
		// System.out.println("Sapo-Broker (kind of) performance test");
		for (DestinationType destinationType : destinationTypes)
		{
			// System.out.println("- Destination Type: "+ destinationType);
			for (int messageSize : messageSizes)
			{
				for (NetProtocolType protocolType : protocolTypes)
				{
					// System.out.println("	- Protocol Type: "+ protocolType);
					System.out.println(String.format("[ %s | %s | %s]", destinationType, protocolType, messageSize));
					System.out.println("----------------------------------------------------------------------------------------------------------------------------");
					for (int index = 0; index != clients.length; ++index)
					{
						double nano2second = (1000 * 1000 * 1000); // nanos
						Test test = new Test(destinationType, protocolType, messageSize, clients[index][0], clients[index][1], clients[index][2]);
						double nanos = (double) test.run();
						double totalNrOfMessagesSent = test.getNrOfMessages() * clients[index][0];
						double timePerMsg = ((((double) nanos)) / totalNrOfMessagesSent) / nano2second;
						// double messagesPerSecond = (nanos * (1000*1000*1000)) / (double) nanos; // totalNrOfMessagesSent / (((double) nanos/10e-9));
						double messagesPerSecond = 1 / timePerMsg;
						String result = String.format("--------> Producers: %s, Local Consumers: %s, Remote Consumers: %s. Messages: %s.Time: %s (s). Time per message: %s (s). Messages per second: %s\n", clients[index][0], clients[index][1], clients[index][2], totalNrOfMessagesSent, nanos / nano2second, timePerMsg, messagesPerSecond);
						System.out.println(result);
					}
				}
			}
		}
		System.out.println("Done!");
	}
}
