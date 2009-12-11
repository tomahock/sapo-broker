package pt.com.broker.performance.distributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class TestManager implements BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(TestManager.class);

	private static String TEST_MANAGEMENT_TOPIC_BASE = "/topic/perf-test/management";
	public static String TEST_MANAGEMENT_TOPIC_ACTION = TEST_MANAGEMENT_TOPIC_BASE + "/action/";
	public static String TEST_MANAGEMENT_TOPIC_RESULT = TEST_MANAGEMENT_TOPIC_BASE + "/result/";

	private BrokerClient brokerClient;

	private HashMap<String, DistTestParams> tests = new HashMap<String, DistTestParams>();

	private HashMap<String, List<TestResult>> results = new HashMap<String, List<TestResult>>();

	public static void main(String[] args) throws Throwable
	{
		TestManager testManager = new TestManager();
		testManager.addTests();

		final DistTestCliArgs cargs = CliFactory.parseArguments(DistTestCliArgs.class, args);

		System.out.println(String.format("Test manger running..."));

		testManager.start(cargs.getHost(), cargs.getPort());

		System.out.println(String.format("Tests ended!"));

		// TODO: show results
	}

	private void start(String host, int port)
	{
		try
		{
			brokerClient = new BrokerClient(host, port);

			NetSubscribe netSubscribe = new NetSubscribe(TEST_MANAGEMENT_TOPIC_RESULT + ".*", DestinationType.TOPIC);

			brokerClient.addAsyncConsumer(netSubscribe, this);

			for (String testName : tests.keySet())
			{
				DistTestParams distTestParams = tests.get(testName);
				executeTest(distTestParams);
				Sleep.time(2000);
			}
		}
		catch (Throwable e)
		{
			log.error("Tests failed!", e);
		}
	}

	private volatile CountDownLatch testsCountDown;

	private void consumerEnded(TestResult result)
	{
		System.out.println("Consumer ended");
		synchronized (results)
		{
			List<TestResult> resultsList = results.get(result.getTestName());
			resultsList.add(result);
		}

		testsCountDown.countDown();
	}

	private void producerEnded(TestResult result)
	{
		System.out.println("Producer ended");
		testsCountDown.countDown();
	}

	private void addTests()
	{
		int[] consumersCount = new int[] { 1, 2, 4 };

		int testCount = 1;
		for (int consumers : consumersCount)
		{
			DistTestParams distTestParams = null;
			//Sleep.time(5000);
			for (int size = 256; size < (8 * 1000); size*= 2)
			{
				System.out.println("TestManager.addTests() - size -> " + size);
				distTestParams = new DistTestParams(String.format("test_%s_%s_%s" ,(testCount++)+"", size+"", consumers+""), "/topic/perf", DestinationType.TOPIC, size, 1000);
				distTestParams.getProducers().add("producer1");

				for(int i= 0; i != consumers; ++i)
				{
					distTestParams.getConsumers().add("consumer"+ (i+1));
				}
				tests.put(distTestParams.getTestName(), distTestParams);
				
				System.out.println("Test added: " + distTestParams.getTestName());
			}
		}
	}

	@Override
	public boolean isAutoAck()
	{
		return false;
	}

	@Override
	public void onMessage(NetNotification message)
	{
		byte[] payload = message.getMessage().getPayload();

		TestResult result = TestResult.deserialize(payload);

		if (result.getActorType() == TestResult.ActorType.Consumer)
		{
			consumerEnded(result);
		}
		else
		{
			producerEnded(result);
		}
	}

	private void executeTest(DistTestParams distTestParams)
	{
		testsCountDown = new CountDownLatch(distTestParams.getConsumers().size() + distTestParams.getProducers().size());

		synchronized (results)
		{
			results.put(distTestParams.getTestName(), new ArrayList<TestResult>(distTestParams.getConsumers().size()));
		}

		byte[] serializedData = distTestParams.serialize();

		NetBrokerMessage netBrokerMsg = new NetBrokerMessage(serializedData);

		for (String consumer : distTestParams.getConsumers())
		{
			brokerClient.publishMessage(netBrokerMsg, TEST_MANAGEMENT_TOPIC_ACTION + consumer);
		}
		// wait for 1s
		Sleep.time(1000);
		for (String producer : distTestParams.getProducers())
		{
			brokerClient.publishMessage(netBrokerMsg, TEST_MANAGEMENT_TOPIC_ACTION + producer);
		}

		try
		{
			testsCountDown.await(); // Eventually use a timeout to prevent deadlocks in case a actor fails
			System.out.println(String.format("Test '%s' ended", distTestParams.getTestName()));
		}
		catch (InterruptedException e)
		{
			log.error("InterruptedException while waiting on testCountDown", e);
		}

	}
}
