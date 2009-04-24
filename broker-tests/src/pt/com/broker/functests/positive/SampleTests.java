package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.Test;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.GenericBrokerListener;
import pt.com.broker.functests.helpers.NotificationConsequence;
import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetSubscribe;

public class SampleTests
{

	public static Test t = new BrokerTest("simple pub/sub test")
	{
		String topicName = "/topic/foo";
		byte[] data = "test".getBytes();

		GenericBrokerListener brokerListener = new GenericBrokerListener(NetAction.DestinationType.TOPIC);

		BrokerClient consumer;

		@Override
		public void build()
		{
			this.addPrerequisite(new Prerequisite("Subscription")
			{
				public Step run() throws Exception
				{
					try
					{
						consumer = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mysniffer", getEncodingProtocolType());

						NetSubscribe subscribe = new NetSubscribe("/topic/.*", NetAction.DestinationType.TOPIC);
						consumer.addAsyncConsumer(subscribe, brokerListener);

						setDone(true);
						setSucess(true);
					}
					catch (Throwable t)
					{
						throw new Exception(t);
					}
					return this;
				}

			});

			this.setAction(new Action("Publish", "producer")
			{
				public Step run() throws Exception
				{

					try
					{
						BrokerClient bk = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher", getEncodingProtocolType());
						NetBrokerMessage brokerMessage = new NetBrokerMessage(data);

						bk.publishMessage(brokerMessage, topicName);

						bk.close();

						setDone(true);
						setSucess(true);
					}
					catch (Throwable t)
					{
						throw new Exception(t);
					}
					return this;

				}
			});

			NotificationConsequence notConsequence = new NotificationConsequence("Consume", "consumer", brokerListener);
			notConsequence.setDestination(topicName);
			notConsequence.setSubscription("/topic/.*");
			notConsequence.setDestinationType(NetAction.DestinationType.TOPIC);
			notConsequence.setMessagePayload(data);

			this.addConsequences(notConsequence);

			this.addEpilogue(new Epilogue("Epilogue")
			{
				public Step run() throws Exception
				{
					
					try
					{
						consumer.unsubscribe(NetAction.DestinationType.TOPIC, "/topic/.*");
						consumer.close();

						setDone(true);
						setSucess(true);

					}
					catch (Throwable t)
					{
						throw new Exception(t);
					}
					return this;
				}
			});
		}
	};
}
