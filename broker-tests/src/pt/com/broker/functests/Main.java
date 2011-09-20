package pt.com.broker.functests;

import org.caudexorigo.Shutdown;
import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.MultipleGenericVirtualQueuePubSubTest;
import pt.com.broker.functests.negative.AcceptedTest;
import pt.com.broker.functests.negative.AccessDeniedTest;
import pt.com.broker.functests.negative.BadEncodingTypeTest;
import pt.com.broker.functests.negative.BadEncodingVersionTest;
import pt.com.broker.functests.negative.EmptyDestinationNameInAck;
import pt.com.broker.functests.negative.EmptyDestinationNameInPoll;
import pt.com.broker.functests.negative.EmptyDestinationNameInPublication;
import pt.com.broker.functests.negative.EmptyDestinationNameInSubscription;
import pt.com.broker.functests.negative.FaultTest;
import pt.com.broker.functests.negative.InvalidAuthChannelTypeTest;
import pt.com.broker.functests.negative.InvalidDestinationName;
import pt.com.broker.functests.negative.InvalidDestinationNameInPublishTest;
import pt.com.broker.functests.negative.InvalidDestinationType;
import pt.com.broker.functests.negative.InvalidMessageTest;
import pt.com.broker.functests.negative.InvalidRandomMessageTest;
import pt.com.broker.functests.negative.MessageSizeBiggerThanMessageTest;
import pt.com.broker.functests.negative.MessegeOversizedTest;
import pt.com.broker.functests.negative.NotificationTest;
import pt.com.broker.functests.negative.PongTest;
import pt.com.broker.functests.negative.TimeoutPollTest;
import pt.com.broker.functests.negative.TotallyInvalidRandomMessageTest;
import pt.com.broker.functests.positive.DeferredDeliveryQueueTest;
import pt.com.broker.functests.positive.Multiple1NTopic;
import pt.com.broker.functests.positive.Multiple1NTopicRemote;
import pt.com.broker.functests.positive.MultipleN1Queue;
import pt.com.broker.functests.positive.MultipleN1QueueRemote;
import pt.com.broker.functests.positive.MultipleN1Topic;
import pt.com.broker.functests.positive.MultipleN1TopicRemote;
import pt.com.broker.functests.positive.MultipleNNQueue;
import pt.com.broker.functests.positive.MultipleNNQueueRemote;
import pt.com.broker.functests.positive.MultipleNNTopic;
import pt.com.broker.functests.positive.MultipleNNTopicRemote;
import pt.com.broker.functests.positive.PingTest;
import pt.com.broker.functests.positive.PollNoWaitTest;
import pt.com.broker.functests.positive.PollTest;
import pt.com.broker.functests.positive.PollVirtualQueueTest;
import pt.com.broker.functests.positive.QueueTest;
import pt.com.broker.functests.positive.QueueTestDist;
import pt.com.broker.functests.positive.TopicNameSpecified;
import pt.com.broker.functests.positive.TopicNameSpecifiedDist;
import pt.com.broker.functests.positive.TopicNameWildcard;
import pt.com.broker.functests.positive.TopicNameWildcardDist;
import pt.com.broker.functests.positive.TopicPubSubWithActionId;
import pt.com.broker.functests.positive.UdpQueuePublishTest;
import pt.com.broker.functests.positive.UdpTopicPublishTest;
import pt.com.broker.functests.positive.VirtualQueueNameSpecified;
import pt.com.broker.functests.positive.VirtualQueueNameSpecifiedRemote;
import pt.com.broker.functests.positive.VirtualQueueTopicNameWildcard;
import pt.com.broker.functests.positive.VirtualQueueTopicNameWildcardRemote;
import pt.com.broker.types.NetProtocolType;

public class Main
{
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args)
	{
		// Positive Tests

		// NetProtocolType[] protoTypes = new NetProtocolType[] {NetProtocolType.SOAP, NetProtocolType.PROTOCOL_BUFFER , NetProtocolType.THRIFT, NetProtocolType.SOAP_v0};

		NetProtocolType[] protoTypes = new NetProtocolType[] { NetProtocolType.SOAP, NetProtocolType.PROTOCOL_BUFFER, NetProtocolType.THRIFT, NetProtocolType.JSON, NetProtocolType.SOAP_v0 };

		TestsResults testResults = new TestsResults();

		CliArgs cargs = null;
		try
		{
			cargs = CliFactory.parseArguments(CliArgs.class, args);
		}
		catch (Throwable e)
		{
			System.out.println("Invalid arguments: " + e.getMessage());
			return;
		}

		boolean runAll = cargs.getAll() == 1;
		boolean runPositive = cargs.getPositive() == 1;
		boolean runNegative = cargs.getNegative() == 1;
		boolean runTopic = cargs.getTopic() == 1;
		boolean runQueue = cargs.getQueue() == 1;
		boolean runVirtualQueue = cargs.getVirtualQueue() == 1;
		boolean runSSLandAuth = cargs.getSslAndAuthentication() == 1;
		boolean runUdp = cargs.getUdp() == 1;

		int numberOfTests = cargs.getNumberOfRuns();

		ConfigurationInfo.init();

		for (NetProtocolType protoType : protoTypes)
		{
			System.out.println(String.format(" ---> Using %s encoding protocol", protoType));

			testResults.addProperty("Encoding", protoType.toString());

			BrokerTest.setDefaultimeout(12 * 1000);
			BrokerTest.setDefaultEncodingProtocolType(protoType);
			BrokerTest.setDefaultDataLenght(512);

			if (runAll || runPositive)
			{
				new PingTest().run(numberOfTests, testResults);
				new DeferredDeliveryQueueTest().run(numberOfTests, testResults);
			}

			if (runAll || runPositive || runTopic)
			{
				new TopicNameSpecified().run(numberOfTests, testResults);
				new TopicPubSubWithActionId().run(numberOfTests, testResults);
				new TopicNameWildcard().run(numberOfTests, testResults);

				new TopicNameWildcardDist().run(numberOfTests, testResults);
				new TopicNameSpecifiedDist().run(numberOfTests, testResults);

				new MultipleN1Topic().run(numberOfTests, testResults);
				new Multiple1NTopic().run(numberOfTests, testResults);
				new MultipleNNTopic().run(numberOfTests, testResults);
				new MultipleN1TopicRemote().run(numberOfTests, testResults);
				new Multiple1NTopicRemote().run(numberOfTests, testResults);
				new MultipleNNTopicRemote().run(numberOfTests, testResults);
			}

			if (runAll || runPositive || runQueue)
			{
				new QueueTest().run(numberOfTests, testResults);
				new PollTest().run(numberOfTests, testResults);
				new PollVirtualQueueTest().run(numberOfTests, testResults);
				new PollNoWaitTest().run(numberOfTests, testResults);

				new QueueTestDist().run(numberOfTests, testResults);

				new MultipleN1Queue().run(numberOfTests, testResults);
				new MultipleNNQueue().run(numberOfTests, testResults);

				new MultipleN1QueueRemote().run(numberOfTests, testResults);
				new MultipleNNQueueRemote().run(numberOfTests, testResults);

				new MultipleGenericVirtualQueuePubSubTest().run(numberOfTests, testResults);
			}

			if (runAll || runPositive || runVirtualQueue)
			{
				new VirtualQueueNameSpecified().run(numberOfTests, testResults);
				new VirtualQueueTopicNameWildcard().run(numberOfTests, testResults);
				new VirtualQueueNameSpecifiedRemote().run(numberOfTests, testResults);
				new VirtualQueueTopicNameWildcardRemote().run(numberOfTests, testResults);
			}

			if (runAll || runPositive || runUdp)
			{
				new UdpTopicPublishTest().run(numberOfTests, testResults);
				new UdpQueuePublishTest().run(numberOfTests, testResults);
			}

			// Negative Tests

			if (runAll || runNegative)
			{
				new MessegeOversizedTest().run(numberOfTests, testResults);
				new BadEncodingTypeTest().run(numberOfTests, testResults);
				new BadEncodingVersionTest().run(numberOfTests, testResults);

				new EmptyDestinationNameInSubscription().run(numberOfTests, testResults);
				new EmptyDestinationNameInPublication().run(numberOfTests, testResults);
				new EmptyDestinationNameInPoll().run(numberOfTests, testResults);
				new EmptyDestinationNameInAck().run(numberOfTests, testResults);

				new InvalidMessageTest().run(numberOfTests, testResults);
				new InvalidRandomMessageTest().run(numberOfTests, testResults);
				new TotallyInvalidRandomMessageTest().run(numberOfTests, testResults);
				new MessageSizeBiggerThanMessageTest().run(numberOfTests, testResults);
				new NotificationTest().run(numberOfTests, testResults);
				new PongTest().run(numberOfTests, testResults);
				new FaultTest().run(numberOfTests, testResults);
				new AcceptedTest().run(numberOfTests, testResults);
				new InvalidDestinationName().run(numberOfTests, testResults);
				new InvalidDestinationType().run(numberOfTests, testResults);
				new InvalidDestinationNameInPublishTest().run(numberOfTests, testResults);
				new AccessDeniedTest().run(numberOfTests, testResults);
				new InvalidAuthChannelTypeTest().run(numberOfTests, testResults);
				new TimeoutPollTest().run(numberOfTests, testResults);
			}
			// for(Class testClass : ConfigurationInfo.getTestClasses())
			// {
			// Test t = createInstance(testClass);
			// t.run(numberOfTests, testResults);
			// }
		}

		System.out.println();
		System.out.println("Functional tests ended!");
		System.out.println("	Total tests: " + testResults.getTotalTests());
		System.out.println("	Successful tests: " + testResults.getPositiveTestsCount());
		System.out.println("	Failed tests: " + testResults.getFailedTestsCount());
		for (String testName : testResults.getFailedTests())
			System.out.println("		- " + testName);
		System.out.println("	Skipped tests: " + testResults.getSkippedTestsCount());
		for (String testName : testResults.getSkippedTests())
			System.out.println("		- " + testName);
		System.exit(0);
	}

	private static Test createInstance(Class testClass)
	{
		try
		{
			Object newInstance = testClass.newInstance();
			return (Test) newInstance;
		}
		catch (Exception e)
		{
			log.error(String.format("Failed to create a instance of type: %s", testClass.getName()), e);
			Shutdown.now();
		}
		return null;
	}
}