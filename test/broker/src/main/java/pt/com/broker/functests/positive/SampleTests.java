package pt.com.broker.functests.positive;


import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.Test;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.NotificationConsequence;
import pt.com.broker.types.*;

public class SampleTests
{




	public static Test t = new BrokerTest("simple pub/sub test")
	{
        NetNotification[] notifications = {null};

		String topicName = "/topic/foo";
        NotificationListenerAdapter brokerListener = new NotificationListenerAdapter() {
            @Override
            public boolean onMessage(NetNotification message, HostInfo host) {
                return true;
            }
        };

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
						consumer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), getEncodingProtocolType());

						NetSubscribe subscribe = new NetSubscribe("/topic/.*", NetAction.DestinationType.TOPIC);
						consumer.subscribe(subscribe, brokerListener);



                        consumer.close();
                        Thread.sleep(2000);

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
						BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), getEncodingProtocolType());
						NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

						bk.publish(brokerMessage, topicName, NetAction.DestinationType.TOPIC).get();

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

			NotificationConsequence notConsequence = new NotificationConsequence("Consume", "consumer", notifications );
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
