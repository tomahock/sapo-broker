package pt.com.broker.functests.positive;

import java.util.Arrays;

import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class PollVirtualQueueTest extends BrokerTest
{
	private String baseName = RandomStringUtils.randomAlphanumeric(10);
	private String topicName;
	private String queueName;

	public PollVirtualQueueTest()
	{
		super("Virtual Poll test");
		baseName = RandomStringUtils.randomAlphanumeric(10);
		topicName = String.format("/topic/%s", baseName);
		queueName = "app@" + topicName;
	}

	@Override
	protected void build() throws Throwable
	{
		setAction(new Action("Poll Test", "Producer")
		{

			@Override
			public Step run() throws Exception
			{
				try
				{
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());

					NetSubscribe subscribe = new NetSubscribe(queueName, DestinationType.VIRTUAL_QUEUE);
					bk.addAsyncConsumer(subscribe, new BrokerListener()
					{
						@Override
						public boolean isAutoAck()
						{
							return false;
						}

						@Override
						public void onMessage(NetNotification message)
						{
						}

					});
					Sleep.time(150);

					bk.unsubscribe(DestinationType.VIRTUAL_QUEUE, queueName);

					Sleep.time(150);

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

		addConsequences(new Consequence("Poll Test", "Consumer")
		{
			@Override
			public Step run() throws Exception
			{
				try
				{
					BrokerClient bk = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());

					NetNotification msg = bk.poll(queueName);

					bk.acknowledge(msg);

					bk.close();

					if (msg.getMessage() == null)
					{
						setReasonForFailure("Broker Message is null");
						return this;
					}
					if (msg.getMessage().getPayload() == null)
					{
						setReasonForFailure("Message payload is null");
						return this;
					}

					if (!Arrays.equals(msg.getMessage().getPayload(), getData()))
					{
						setReasonForFailure("Message payload is different from expected");
						return this;
					}

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
}
