package pt.com.broker.performance.distributed;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import org.caudexorigo.text.StringUtils;
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

	private static String TEST_MANAGEMENT_BASE = "/perf-test/management";
	public static String TEST_MANAGEMENT_ACTION = TEST_MANAGEMENT_BASE + "/action/";
	public static String TEST_MANAGEMENT_RESULT = TEST_MANAGEMENT_BASE + "/result";

	private BrokerClient brokerClient;

	private TreeMap<String, DistTestParams> tests = new TreeMap<String, DistTestParams>();

	private TreeMap<String, List<TestResult>> results = new TreeMap<String, List<TestResult>>();

	public static void main(String[] args) throws Throwable
	{
		TestManager testManager = new TestManager();
		testManager.addTests();
		

		final DistTestCliArgs cargs = CliFactory.parseArguments(DistTestCliArgs.class, args);

		System.out.println(String.format("Test manger running..."));

		testManager.start(cargs.getHost(), cargs.getPort());

		System.out.println(String.format("Tests ended!"));

		for (String testname : testManager.results.keySet())
		{
			ShowTestResult(testname, testManager.results.get(testname));
		}
	}

	private static void ShowTestResult(String testname, List<TestResult> testResults)
	{
		final double nano2second = (1000 * 1000 * 1000);

		System.out.println("\n--------------------------------------------------\n");
		System.out.println("TEST: " + testname);

		double timePerMsgAcc = 0;
		double messagesPerSecondAcc = 0;

		for (TestResult tRes : testResults)
		{
			double timePerMsg = ((((double) tRes.getTime())) / tRes.getMessages()) / nano2second;
			double messagesPerSecond = 1 / timePerMsg;
			System.out.println(String.format("Consumer: %s, Messages: %s, Time: %s, Time/message: %s, Message/second: %s", tRes.getActorName(), tRes.getMessages(), tRes.getTime(), timePerMsg, messagesPerSecond));

			timePerMsgAcc += timePerMsg;
			messagesPerSecondAcc += messagesPerSecond;
		}

		System.out.println(String.format("AVERAGE - Time/message: %s, Message/second: %s", timePerMsgAcc / testResults.size(), messagesPerSecondAcc / testResults.size()));

	}

	private void start(String host, int port)
	{
		try
		{
			brokerClient = new BrokerClient(host, port);

			NetSubscribe netSubscribe = new NetSubscribe(TEST_MANAGEMENT_RESULT, DestinationType.QUEUE);

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
		System.out.println("Consumer ended: " + result.getActorName());
		synchronized (results)
		{
			List<TestResult> resultsList = results.get(result.getTestName());
			resultsList.add(result);
		}

		testsCountDown.countDown();
	}

	private void producerEnded(TestResult result)
	{
		System.out.println("Producer ended: " + result.getActorName());
		testsCountDown.countDown();
	}

	private void addTests()
	{
		final int NUMBER_OF_MESSAGES = 1000;
		final int ERROR_DELTA = 100;
		
		int[] consumersCount = new int[] { /*1, */2, 4 };

		int testCount = 1;

		for (DestinationType dt : new DestinationType[]{DestinationType.TOPIC, DestinationType.QUEUE} )
		{
			for (int consumers : consumersCount)
			{
				if(dt == DestinationType.QUEUE)
				{
					if( (NUMBER_OF_MESSAGES%consumers) != 0)
					{
						continue;
					}
				}
				
				DistTestParams distTestParams = null;

				for (int size = 256; size < (8 * 1000); size *= 2)
				{
					System.out.println("TestManager.addTests() - size -> " + size);
					
					int messagesToReceive = (dt == DestinationType.TOPIC)? NUMBER_OF_MESSAGES: (NUMBER_OF_MESSAGES/consumers);
					int messagesToSend= (dt == DestinationType.TOPIC)? NUMBER_OF_MESSAGES: (NUMBER_OF_MESSAGES + ERROR_DELTA);
					
					String randName = RandomStringUtils.randomAlphanumeric(15);
										
					distTestParams = new DistTestParams(String.format("test_%s_%s_%s_%s", dt, (testCount++) + "", size + "", consumers + ""),
							String.format("/%s/perf/%s", dt.toString().toLowerCase(), randName), dt, size, messagesToReceive, messagesToSend);
					distTestParams.getProducers().add("producer1");

					for (int i = 0; i != consumers; ++i)
					{
						distTestParams.getConsumers().add("consumer" + (i + 1));
					}
					tests.put(distTestParams.getTestName(), distTestParams);

					System.out.println( String.format("Test added: %s",  distTestParams.getTestName()));
				}
			}

		}
	}

	@Override
	public boolean isAutoAck()
	{
		return true;
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

		System.out.println(String.format("\nStarting test '%s'", distTestParams.getTestName()));

		synchronized (results)
		{
			results.put(distTestParams.getTestName(), new ArrayList<TestResult>(distTestParams.getConsumers().size()));
		}

		byte[] serializedData = distTestParams.serialize();

		NetBrokerMessage netBrokerMsg = new NetBrokerMessage(serializedData);

		for (String consumer : distTestParams.getConsumers())
		{
			brokerClient.enqueueMessage(netBrokerMsg, TEST_MANAGEMENT_ACTION + consumer);
		}
		// wait for 1s
		Sleep.time(1000);
		for (String producer : distTestParams.getProducers())
		{
			brokerClient.enqueueMessage(netBrokerMsg, TEST_MANAGEMENT_ACTION + producer);
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
