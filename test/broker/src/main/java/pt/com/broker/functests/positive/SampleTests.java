package pt.com.broker.functests.positive;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.Test;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.GenericBrokerListener;
import pt.com.broker.functests.helpers.NotificationConsequence;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetSubscribe;

public class SampleTests
{

	public static Test t = new BrokerTest("simple pub/sub test")
	{
		String topicName = "/topic/foo";
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
						consumer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());

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
						BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
						NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

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
			notConsequence.setMessagePayload(getData());

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
