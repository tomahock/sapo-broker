package pt.com.broker.functests.helpers;

import java.util.ArrayList;
import java.util.List;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.Test;
import pt.com.types.NetAction;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetSubscribe;
import pt.com.types.NetAction.DestinationType;

public class MultipleGenericPubSubTest extends BrokerTest
{
	public static class TestClientInfo
	{
		public BrokerClient brokerCLient;
		public MultipleNotificationsBrokerListener brokerListenter;
		public int numberOfExecutions;
	}

	private String destinationName = "/topic/foo";
	private String subscriptionName = "/topic/foo";

	private byte[] data = "This is the data to be transferred.".getBytes();

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

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher", this.getEncodingProtocolType());
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

			tci.brokerCLient = new BrokerClient("127.0.0.1", 3323, "tcp://mycompany.com/mypublisher", this.getEncodingProtocolType());
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
						tci.brokerCLient.addAsyncConsumer(subscribe, tci.brokerListenter);
					}

					Sleep.time(1000);
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
						NetBrokerMessage brokerMessage = new NetBrokerMessage(data);

						if (getDestinationType().equals(DestinationType.TOPIC))
						{
							tci.brokerCLient.publishMessage(brokerMessage, getDestinationName());
						}
						else
						{
							tci.brokerCLient.enqueueMessage(brokerMessage, getDestinationName());
						}

					}

					for (TestClientInfo tci : getInfoProducers())
					{
						tci.brokerCLient.close();
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
			notConsequence.setDestination(getDestinationName()/*getConsumerDestinationType().equals(DestinationType.VIRTUAL_QUEUE)? "xpto@" + getDestinationName() :getDestinationName()*/);
			notConsequence.setSubscription(getSubscriptionName());
			notConsequence.setDestinationType(getConsumerDestinationType().equals(DestinationType.VIRTUAL_QUEUE)? DestinationType.QUEUE : getConsumerDestinationType());
			notConsequence.setMessagePayload(data);

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
						tci.brokerCLient.unsubscribe(NetAction.DestinationType.TOPIC, getSubscriptionName());
						Sleep.time(1000);
						tci.brokerCLient.close();
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
}
