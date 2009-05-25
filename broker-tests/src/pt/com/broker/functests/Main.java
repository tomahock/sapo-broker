package pt.com.broker.functests;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.types.NetProtocolType;
import pt.com.broker.functests.helpers.*;
import pt.com.broker.functests.negative.*;
import pt.com.broker.functests.positive.*;

public class Main
{
	/*
	public static class HelloWorldTest extends Test
	{
		public HelloWorldTest(){
			super("Hello World");
		}
		
		@Override
		protected void build()
		{
			this.addPrerequisite(new Prerequisite("simple prerequisite")
			{
				public Step run() throws Exception
				{
					System.out.println("I'm a prerequisite and I'm running!!");
					// consumer.subscribe!

					Thread.sleep(3000);

					setDone(true);
					setSucess(true);

					return this;
				}

			});

			this.setAction(new Action("print", "producer")
			{
				public Step run() throws Exception
				{
					System.out.println("I'm an action and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});

			this.addConsequences(new Consequence("Consequence 1", "consumer")
			{
				public Step run() throws Exception
				{
					System.out.println("I'm an consquence and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});

			this.addConsequences(new Consequence("Consequence 2", "consumer")
			{
				public Step run() throws Exception
				{
					System.out.println("I'm another consquence and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});

			this.addEpilogue(new Epilogue("Epilogue")
			{
				public Step run() throws Exception
				{
					System.out.println("I'm an eplilogue and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});
		}
	}
	*/

	public static void main(String[] args)
	{
//		new HelloWorldTest().run();

//		// Positive Tests
		
		NetProtocolType[] protoTypes = new NetProtocolType[]{NetProtocolType.SOAP, NetProtocolType.PROTOCOL_BUFFER, NetProtocolType.THRIFT};
		
		for(NetProtocolType protoType : protoTypes)
		{
		
			System.out.println(String.format(" ---> Using %s encoding protocol", protoType));
			
			BrokerTest.setDefaultimeout(12*1000);
			BrokerTest.setDefaultEncodingProtocolType(protoType);
			BrokerTest.setDefaultDataLenght(1024);
			
			int numberOfTests = 1;
	
//			new PingTest().run(numberOfTests);
//			
//			new TopicNameSpecified().run(numberOfTests);
//			new TopicPubSubWithActionId().run(numberOfTests);
//			new TopicNameWildcard().run(numberOfTests);
//
//			new QueueTest().run(numberOfTests);
//			new PollTest().run(numberOfTests);
//
//			new TopicNameWildcardDist().run(numberOfTests);
//			new TopicNameSpecifiedDist().run(numberOfTests);
//			new QueueTestDist().run(numberOfTests);
//
//			new MultipleN1Topic().run(numberOfTests);
//			new Multiple1NTopic().run(numberOfTests);
//			new MultipleNNTopic().run(numberOfTests);
//			new MultipleN1TopicRemote().run(numberOfTests);
//			new Multiple1NTopicRemote().run(numberOfTests);
//			new MultipleNNTopicRemote().run(numberOfTests);
//
//			new MultipleN1Queue().run(numberOfTests);
//			new MultipleNNQueue().run(numberOfTests);
//
//			new MultipleN1QueueRemote().run(numberOfTests);
//			new MultipleNNQueueRemote().run(numberOfTests);
//
//			new MultipleGenericVirtualQueuePubSubTest().run(numberOfTests);
//
//			new VirtualQueueNameSpecified().run(numberOfTests);
//			new VirtualQueueTopicNameWildcard().run(numberOfTests);
//			new VirtualQueueNameSpecifiedRemote().run(numberOfTests);
//			new VirtualQueueTopicNameWildcardRemote().run(numberOfTests);
//			new SslTopicNameSpeficied().run(numberOfTests);			
//			new AuthenticationTopicSslTopicNameSpecified().run(numberOfTests);
//			new DBRolesAuthenticationTest().run(numberOfTests);
//		
//			//Negative Tests
//		
//			new MessegeOversizedTest().run(numberOfTests);
//			new BadEncodingTypeTest().run(numberOfTests);
//			new BadEncodingVersionTest().run(numberOfTests);
//
//			new InvalidMessageTest().run(numberOfTests);
//			new InvalidRandomMessageTest().run(numberOfTests);
//			new TotallyInvalidRandomMessageTest().run(numberOfTests);
//			new MessageSizeBiggerThanMessageTest().run(numberOfTests);
//			new NotificationTest().run(numberOfTests);
//			new PongTest().run(numberOfTests);
//			new FaultTest().run(numberOfTests);
//			new FaultWithActionIdTest().run(numberOfTests);
//			new AcceptedTest().run(numberOfTests);
//			new InvalidDestinationName().run(numberOfTests);
//			new InvalidDestinationType().run(numberOfTests);
//			new InvalidDestinationNameInPublishTest().run(numberOfTests);
//			new AccessDeniedTest().run(numberOfTests);
//			new AuthenticationFailedTest().run(numberOfTests);
			new UnknownAuthTypeFailedTest().run(numberOfTests);
		
		}
		
		System.out.println("Is it ending?");
	}

}
