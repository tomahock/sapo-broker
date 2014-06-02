package pt.com.broker.functests.helpers;

import org.caudexorigo.concurrent.Sleep;


import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.types.*;
import pt.com.broker.types.NetAction.DestinationType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GenericPubSubTest extends BrokerTest
{
	private String destinationName = "/topic/foo";
	private String subscriptionName = "/topic/foo";

	private DestinationType destinationType = DestinationType.TOPIC;

	private NotificationListenerAdapter brokerListener;

	private BrokerClient infoConsumer;
	private BrokerClient infoProducer;

	protected boolean constructionFailed = false;
	protected Throwable reasonForFailure;

	public GenericPubSubTest()
	{
		this("GenericPubSubTest");
	}

    NetNotification[] last = {null};


	public GenericPubSubTest(String testName)
	{
		super(testName);
		try
		{
			infoConsumer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port() , this.getEncodingProtocolType());
            infoConsumer.connect();


			infoProducer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port() , this.getEncodingProtocolType());
            infoProducer.connect();


		}
		catch (Throwable t)
		{
			constructionFailed = true;
			reasonForFailure = t;
		}

	}

    @Override
    protected void end() {
        try {
            infoConsumer.close();
            infoProducer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public void build() throws Throwable
	{
		if (constructionFailed)
			throw reasonForFailure;

		if (getBrokerListener() == null)
		{
			brokerListener = new NotificationListenerAdapter() {
                @Override
                public boolean onMessage(NetNotification message, HostInfo host) {

                    last[0] = message;

                    return true;
                }
            };
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

					getInfoConsumer().subscribe(subscribe,getBrokerListener()).get();

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

					NetBrokerMessage brokerMessage = new NetBrokerMessage(getData());

					Future f = getInfoProducer().publish(brokerMessage, getDestinationName(), getDestinationType());

                    f.get();


                    Thread.sleep(2000);

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



		NotificationConsequence notConsequence = new NotificationConsequence("Consume", "consumer", last);
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
					getInfoConsumer().unsubscribe(NetAction.DestinationType.TOPIC, getSubscriptionName()).get();

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

	public void setBrokerListener(NotificationListenerAdapter brokerListener)
	{
		this.brokerListener = brokerListener;
	}

	public NotificationListenerAdapter getBrokerListener()
	{
		return brokerListener;
	}

	public void setInfoConsumer(BrokerClient infoConsumer)
	{
		this.infoConsumer = infoConsumer;
	}

	public BrokerClient getInfoConsumer()
	{
		return infoConsumer;
	}

	public void setInfoProducer(BrokerClient infoProducer)
	{
		this.infoProducer = infoProducer;
	}

	public BrokerClient getInfoProducer()
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
