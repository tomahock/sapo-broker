package pt.com.broker.functests.helpers;

import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BaseBrokerClient;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetSubscribe;

public class MultipleGenericPubSubTest extends BrokerTest
{
	public static class TestClientInfo
	{
		public BaseBrokerClient brokerClient;
		public MultipleNotificationsBrokerListener brokerListenter;
		public int numberOfExecutions;
	}

	private String baseName = RandomStringUtils.randomAlphanumeric(10);
	private String destinationName = String.format("/%s/foo", getBaseName());
	private String subscriptionName = String.format("/%s/foo", getBaseName());

	private DestinationType destinationType = DestinationType.TOPIC;
	private DestinationType consumerDestinationType = destinationType;

	private List<TestClientInfo> infoConsumers;
	private List<TestClientInfo> infoProducers;

	public MultipleGenericPubSubTest()
	{
		this("MultipleGenericPubSubTest");
	}

	public MultipleGenericPubSubTest(String testName)
	{
		super(testName);
		infoConsumers = new ArrayList<TestClientInfo>();
		infoProducers = new ArrayList<TestClientInfo>();
	}

	@Override
	public void build() throws Throwable
	{
		addProducers();

		addConsumers();

		if (isConstructionFailed())
			throw getReasonForFailure();

		addPrerequisites();

		addAction();

		addConsequences();

		addEpilogues();
	}

	protected void addConsumers()
	{
		try
		{
			int numberOfExecutions = getInfoProducers().size();

			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", this.getEncodingProtocolType());
			tci.brokerListenter = new MultipleNotificationsBrokerListener(getDestinationType(), numberOfExecutions);
			tci.numberOfExecutions = numberOfExecutions;

			this.addInfoConsumer(tci);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}

	}

	protected void addProducers()
	{
		try
		{
			TestClientInfo tci = new TestClientInfo();

			tci.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", this.getEncodingProtocolType());
			tci.brokerListenter = null;
			tci.numberOfExecutions = 1;

			this.addInfoProducer(tci);
		}
		catch (Throwable t)
		{
			setFailure(t);
		}
	}

	protected void addPrerequisites()
	{
		this.addPrerequisite(new Prerequisite("Subscription")
		{
			public Step run() throws Exception
			{
				try
				{
					NetSubscribe subscribe = new NetSubscribe(getSubscriptionName(), getConsumerDestinationType());
					for (TestClientInfo tci : getInfoConsumers())
					{
						tci.brokerClient.addAsyncConsumer(subscribe, tci.brokerListenter);
					}

					Sleep.time(250);
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

	protected void addAction()
	{
		this.setAction(new Action("Publish", "producer")
		{
			public Step run() throws Exception
			{

				try
				{
					for (TestClientInfo tci : getInfoProducers())
					{
						NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

						if (getDestinationType().equals(DestinationType.TOPIC))
						{
							tci.brokerClient.publishMessage(brokerMessage, getDestinationName());
						}
						else
						{
							tci.brokerClient.enqueueMessage(brokerMessage, getDestinationName());
						}

					}

					for (TestClientInfo tci : getInfoProducers())
					{
						tci.brokerClient.close();
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

	protected void addConsequences()
	{
		for (TestClientInfo tci : getInfoConsumers())
		{
			MultipleNotificationConsequence notConsequence = new MultipleNotificationConsequence("Consume", "consumer", tci.brokerListenter);
			notConsequence.setDestination(getDestinationName());
			notConsequence.setSubscription(getSubscriptionName());

			DestinationType dt = getConsumerDestinationType().equals(DestinationType.VIRTUAL_QUEUE) ? DestinationType.QUEUE : getConsumerDestinationType();

			notConsequence.setDestinationType(dt);
			notConsequence.setMessagePayload(getData());

			this.addConsequences(notConsequence);
		}
	}

	protected void addEpilogues()
	{
		this.addEpilogue(new Epilogue("Epilogue")
		{
			public Step run() throws Exception
			{

				try
				{

					for (TestClientInfo tci : getInfoConsumers())
					{
						tci.brokerClient.unsubscribe(NetAction.DestinationType.TOPIC, getSubscriptionName());
						Sleep.time(250);
						tci.brokerClient.close();
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

	public void setDestinationName(String topicName)
	{
		this.destinationName = topicName;
	}

	public String getDestinationName()
	{
		return destinationName;
	}

	public void setSubscriptionName(String subscriptionName)
	{
		this.subscriptionName = subscriptionName;
	}

	public String getSubscriptionName()
	{
		return subscriptionName;
	}

	public void addInfoConsumer(TestClientInfo infoConsumer)
	{
		this.infoConsumers.add(infoConsumer);
	}

	public List<TestClientInfo> getInfoConsumers()
	{
		return infoConsumers;
	}

	public void addInfoProducer(TestClientInfo infoProducer)
	{
		this.infoProducers.add(infoProducer);
	}

	public List<TestClientInfo> getInfoProducers()
	{
		return infoProducers;
	}

	public void setDestinationType(DestinationType destinationType)
	{
		this.destinationType = destinationType;
	}

	public DestinationType getDestinationType()
	{
		return destinationType;
	}

	public void setConsumerDestinationType(DestinationType consumerDestinationType)
	{
		this.consumerDestinationType = consumerDestinationType;
	}

	public DestinationType getConsumerDestinationType()
	{
		return consumerDestinationType;
	}

	public void setBaseName(String baseName)
	{
		this.baseName = baseName;
	}

	public String getBaseName()
	{
		return baseName;
	}
}
