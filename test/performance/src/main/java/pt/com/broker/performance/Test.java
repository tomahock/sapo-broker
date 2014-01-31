package pt.com.broker.performance;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.performance.conf.ConfigurationInfo;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetProtocolType;

public class Test
{
	private static int defaultNrOfMessages = 10000;
	private static final ExecutorService executer = Executors.newFixedThreadPool(16);

	private int nrOfMessages = getDefaultNrOfMessages();

	private final DestinationType destinationType;
	private final NetProtocolType protocolType;
	private final int messageSize;
	private final int nrProducers;
	private final int nrLocalConsumers;
	private final int nrRemoteConsumers;

	private static final String localAgentAddress;
	private static final int localAgentPort;
	private static final String remotAgentAddress;
	private static final int remoteAgentPort;

	static
	{
		ConfigurationInfo.init();
		localAgentAddress = ConfigurationInfo.getParameter("agent1-host");
		localAgentPort = Integer.parseInt(ConfigurationInfo.getParameter("agent1-port"));
		remotAgentAddress = ConfigurationInfo.getParameter("agent2-host");
		remoteAgentPort = Integer.parseInt(ConfigurationInfo.getParameter("agent2-port"));
	}

	public Test(DestinationType destinationType, NetProtocolType protocolType, int messageSize, int nrProducers, int nrLocalConsumers, int nrRemoteConsumers)
	{
		this.destinationType = destinationType;
		this.protocolType = protocolType;
		this.messageSize = messageSize;
		this.nrProducers = nrProducers;
		this.nrLocalConsumers = nrLocalConsumers;
		this.nrRemoteConsumers = nrRemoteConsumers;
	}

	public long run()
	{
		final String msg = RandomStringUtils.randomAlphanumeric(messageSize);

		int totalConsumers = nrLocalConsumers + nrRemoteConsumers;

		int msgPerClient = getNrOfMessages();

		if (destinationType == DestinationType.QUEUE)
		{
			if (totalConsumers == 0)
			{
				msgPerClient = 0;
			}
			else
			{
				if ((getNrOfMessages() % totalConsumers) != 0)
					throw new RuntimeException("The number of messages consumers must be multiple of the total number of consumers!");
				msgPerClient = getNrOfMessages() / totalConsumers;
			}
		}

		ArrayList<TestActor> clients = new ArrayList<TestActor>(totalConsumers + nrProducers);

		// System.out.println(" - Initializing local consumers...");
		for (int i = 0; i != nrLocalConsumers; ++i)
		{
			BrokerClient bk;
			try
			{
				bk = new BrokerClient(localAgentAddress, localAgentPort, "test app", protocolType);
				Consumer consumer = new Consumer(bk, destinationType, msgPerClient);
				consumer.init();
				clients.add(consumer);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}
		// System.out.println(" - Initializing remote consumers...");
		for (int i = 0; i != nrRemoteConsumers; ++i)
		{
			BrokerClient bk;
			try
			{
				bk = new BrokerClient(remotAgentAddress, remoteAgentPort, "test app", protocolType);
				Consumer consumer = new Consumer(bk, destinationType, msgPerClient);
				consumer.init();
				clients.add(consumer);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		// System.out.println(" - Initializing producers...");
		for (int i = 0; i != nrProducers; ++i)
		{
			BrokerClient bk;
			try
			{
				bk = new BrokerClient(localAgentAddress, localAgentPort, "test app", protocolType);
				Producer producer = new Producer(bk, destinationType, getNrOfMessages(), msg);
				clients.add(producer);
			}
			catch (Throwable e)
			{
				throw new RuntimeException(e);
			}
		}

		long start = System.nanoTime();
		try
		{
			executer.invokeAll(clients, 5, TimeUnit.MINUTES);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		long execTime = System.nanoTime() - start;

		for (TestActor testActor : clients)
		{
			testActor.close();
		}

		return execTime;
	}

	public void setNrOfMessages(int nrOdMessages)
	{
		this.nrOfMessages = nrOdMessages;
	}

	public int getNrOfMessages()
	{
		return nrOfMessages;
	}

	public static void setDefaultNrOdMessages(int nrOdMessages)
	{
		defaultNrOfMessages = nrOdMessages;
	}

	public static int getDefaultNrOfMessages()
	{
		return defaultNrOfMessages;
	}
}
