package pt.com.broker.functests.helpers;

import org.caudexorigo.concurrent.Sleep;

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

public class GenericPubSubTest extends BrokerTest
{
	private String destinationName = "/topic/foo";
	private String subscriptionName = "/topic/foo";

	private DestinationType destinationType = DestinationType.TOPIC;

	private GenericBrokerListener brokerListener;

	private BaseBrokerClient infoConsumer;
	private BaseBrokerClient infoProducer;

	protected boolean constructionFailed = false;
	protected Throwable reasonForFailure;

	public GenericPubSubTest()
	{
		this("GenericPubSubTest");
	}

	public GenericPubSubTest(String testName)
	{
		super(testName);
		try
		{
			infoConsumer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", this.getEncodingProtocolType());
			infoProducer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", this.getEncodingProtocolType());
		}
		catch (Throwable t)
		{
			constructionFailed = true;
			reasonForFailure = t;
		}

	}

	@Override
	public void build() throws Throwable
	{
		if (constructionFailed)
			throw reasonForFailure;

		if (getBrokerListener() == null)
		{
			brokerListener = new GenericBrokerListener(getDestinationType());
		}

		addPrerequisites();

		addAction();

		addConsequences();

		addEpilogues();
	}

	public void setFailure(Throwable throwable)
	{
		constructionFailed = true;
		reasonForFailure = throwable;
	}

	protected void addPrerequisites()
	{
		this.addPrerequisite(new Prerequisite("Subscription")
		{
			public Step run() throws Exception
			{
				try
				{
					NetSubscribe subscribe = new NetSubscribe(getSubscriptionName(), getDestinationType());
					getInfoConsumer().addAsyncConsumer(subscribe, getBrokerListener());

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

					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

					if (getDestinationType().equals(DestinationType.TOPIC))
					{
						getInfoProducer().publishMessage(brokerMessage, getDestinationName());
					}
					else
					{
						getInfoProducer().enqueueMessage(brokerMessage, getDestinationName());
					}

					getInfoProducer().close();

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
		NotificationConsequence notConsequence = new NotificationConsequence("Consume", "consumer", getBrokerListener());
		notConsequence.setDestination(getDestinationName());
		notConsequence.setSubscription(getSubscriptionName());
		notConsequence.setDestinationType(getDestinationType());
		notConsequence.setMessagePayload(getData());

		this.addConsequences(notConsequence);
	}

	protected void addEpilogues()
	{
		this.addEpilogue(new Epilogue("Epilogue")
		{
			public Step run() throws Exception
			{

				try
				{
					getInfoConsumer().unsubscribe(NetAction.DestinationType.TOPIC, getSubscriptionName());

					Sleep.time(250);
					getInfoConsumer().close();

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

	public void setBrokerListener(GenericBrokerListener brokerListener)
	{
		this.brokerListener = brokerListener;
	}

	public GenericBrokerListener getBrokerListener()
	{
		return brokerListener;
	}

	public void setInfoConsumer(BaseBrokerClient infoConsumer)
	{
		this.infoConsumer = infoConsumer;
	}

	public BaseBrokerClient getInfoConsumer()
	{
		return infoConsumer;
	}

	public void setInfoProducer(BaseBrokerClient infoProducer)
	{
		this.infoProducer = infoProducer;
	}

	public BaseBrokerClient getInfoProducer()
	{
		return infoProducer;
	}

	public void setDestinationType(DestinationType destinationType)
	{
		this.destinationType = destinationType;
	}

	public DestinationType getDestinationType()
	{
		return destinationType;
	}
}
