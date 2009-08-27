package pt.com.broker.functests;

import org.caudexorigo.cli.CliFactory;

import pt.com.broker.types.NetProtocolType;
import pt.com.broker.functests.helpers.*;
import pt.com.broker.functests.negative.*;
import pt.com.broker.functests.positive.*;

public class Main
{

	public static void main(String[] args)
	{
		// Positive Tests

		NetProtocolType[] protoTypes = new NetProtocolType[] { NetProtocolType.SOAP, NetProtocolType.PROTOCOL_BUFFER, NetProtocolType.THRIFT };

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

		int numberOfTests = cargs.getNumberOfRuns();

		for (NetProtocolType protoType : protoTypes)
		{

			System.out.println(String.format(" ---> Using %s encoding protocol", protoType));

			BrokerTest.setDefaultimeout(12 * 1000);
			BrokerTest.setDefaultEncodingProtocolType(protoType);
			BrokerTest.setDefaultDataLenght(1024);

			if (runAll || runPositive)
			{
				new PingTest().run(numberOfTests, testResults);
			}

			if (runAll || runSSLandAuth)
			{
				new DBRolesAuthenticationTest().run(numberOfTests, testResults);
				new SslTopicNameSpeficied().run(numberOfTests, testResults);
				new AuthenticationTopicSslTopicNameSpecified().run(numberOfTests, testResults);
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

			// Negative Tests

			if (runAll || runNegative)
			{
				new MessegeOversizedTest().run(numberOfTests, testResults);
				new BadEncodingTypeTest().run(numberOfTests, testResults);
				new BadEncodingVersionTest().run(numberOfTests, testResults);

				new InvalidMessageTest().run(numberOfTests, testResults);
				new InvalidRandomMessageTest().run(numberOfTests, testResults);
				new TotallyInvalidRandomMessageTest().run(numberOfTests, testResults);
				new MessageSizeBiggerThanMessageTest().run(numberOfTests, testResults);
				new NotificationTest().run(numberOfTests, testResults);
				new PongTest().run(numberOfTests, testResults);
				new FaultTest().run(numberOfTests, testResults);
				new FaultWithActionIdTest().run(numberOfTests, testResults);
				new AcceptedTest().run(numberOfTests, testResults);
				new InvalidDestinationName().run(numberOfTests, testResults);
				new InvalidDestinationType().run(numberOfTests, testResults);
				new InvalidDestinationNameInPublishTest().run(numberOfTests, testResults);
				new AccessDeniedTest().run(numberOfTests, testResults);
				new InvalidAuthChannelTypeTest().run(numberOfTests, testResults);
				new AuthenticationFailedTest().run(numberOfTests, testResults);
				new UnknownAuthTypeFailedTest().run(numberOfTests, testResults);
				new TimeoutPollTest().run(numberOfTests, testResults);
			}
		}

		System.out.println("Functional tests ended!");
		System.out.println( "	Total tests: " + testResults.getTotalTests() );
		System.out.println( "	Successful tests: " + testResults.getPositiveTestsCount()); 
		System.out.println( "	Failed tests: " + testResults.getFailedTestsCount());
		for(String testName : testResults.getFailedTests())
			System.out.println( "		- " + testName);
		System.out.println( "	Skipped tests: " + testResults.getSkippedTestsCount());
		for(String testName : testResults.getSkippedTests())
			System.out.println( "		- " + testName);
		System.out.println("	Total tests: " + testResults.getTotalTests());
		System.out.println("	Successful tests: " + testResults.getPositiveTestsCount());
		System.out.println("	Failed tests: " + testResults.getFailedTestsCount());
		for (String testName : testResults.getFailedTests())
			System.out.println("		- " + testName);
		System.out.println("	Skipped tests: " + testResults.getSkippedTests());
		System.exit(0);
	}

}
