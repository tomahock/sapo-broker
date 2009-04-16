package pt.com.broker.functests;

import pt.com.broker.functests.helpers.*;
import pt.com.broker.functests.topicPubSub.*;

public class Main
{

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
				public Step call() throws Exception
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
				public Step call() throws Exception
				{
					System.out.println("I'm an action and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});

			this.addConsequences(new Consequence("Consequence 1", "consumer")
			{
				public Step call() throws Exception
				{
					System.out.println("I'm an consquence and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});

			this.addConsequences(new Consequence("Consequence 2", "consumer")
			{
				public Step call() throws Exception
				{
					System.out.println("I'm another consquence and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});

			this.addEpilogue(new Epilogue("Epilogue")
			{
				public Step call() throws Exception
				{
					System.out.println("I'm an eplilogue and I'm running!!");
					setDone(true);
					setSucess(true);

					return this;
				}
			});
		}
	}

	public static void main(String[] args)
	{
//		new HelloWorldTest().run();

//		new PingTest().run();
//		new TopicNameSpecified().run();
//		new TopicNameWildcard().run();
//		new QueueTest().run();
		new PollTest().run();
//				
//		new TopicNameSpecifiedDist().run();
//		new TopicNameWildcardDist().run();
//		new QueueTestDist().run();
//		
//		new MultipleN1Topic().run();
//		new Multiple1NTopic().run();
//		new MultipleNNTopic().run();
//		new MultipleN1TopicRemote().run();
//		new Multiple1NTopicRemote().run();
//		new MultipleNNTopicRemote().run();
//
//		new MultipleN1Queue().run();
//		new MultipleNNQueue().run();
//		
//		new MultipleN1QueueRemote().run();
//		new MultipleNNQueueRemote().run();
//		
//		new MultipleGenericVirtualQueuePubSubTest().run();

//		new VirtualQueueNameSpecified().run();
//		new VirtualQueueTopicNameWildcard().run();
//		new VirtualQueueNameSpecifiedRemote().run();
//		new VirtualQueueTopicNameWildcardRemote().run();
		
		
		
		System.out.println("Is it ending?");
	}

}
