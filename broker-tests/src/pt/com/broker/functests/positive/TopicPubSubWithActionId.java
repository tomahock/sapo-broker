package pt.com.broker.functests.positive;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.client.AcceptRequest;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.MessageAcceptedListener;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Consequence;
import pt.com.broker.functests.Epilogue;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.functests.helpers.GenericBrokerListener;
import pt.com.broker.functests.helpers.SetValueFuture;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetFault;
import pt.com.broker.types.NetSubscribe;

public class TopicPubSubWithActionId extends BrokerTest
{
	private String destinationName = "/topic/foo";
	private String subscriptionName = "/topic/foo";

	private DestinationType destinationType = DestinationType.TOPIC;

	private GenericBrokerListener brokerListener;

	private BrokerClient infoConsumer;
	private BrokerClient infoProducer;

	private boolean constructionFailed = false;
	private Throwable reasonForFailure;

	private SetValueFuture<Boolean> future = new SetValueFuture<Boolean>();

	public TopicPubSubWithActionId()
	{
		super("GenericPubSubTest");
		try
		{
			infoConsumer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
			infoProducer = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
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

		brokerListener = new GenericBrokerListener(destinationType);

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
					AcceptRequest accReq = new AcceptRequest("123456789", new MessageAcceptedListener()
					{

						@Override
						public void messageAccepted(String actionId)
						{
							future.set(Boolean.TRUE);
						}

						@Override
						public void messageTimedout(String actionId)
						{
							future.set(Boolean.FALSE);
						}

						@Override
						public void messageFailed(NetFault fault)
						{
							future.set(Boolean.FALSE);
						}

					}, 1000);

					NetSubscribe subscribe = new NetSubscribe(subscriptionName, destinationType);
					infoConsumer.addAsyncConsumer(subscribe, brokerListener, accReq);

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

					if (destinationType.equals(DestinationType.TOPIC))
					{
						infoProducer.publishMessage(brokerMessage, destinationName);
					}
					else
					{
						infoProducer.enqueueMessage(brokerMessage, destinationName);
					}

					infoProducer.close();

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

		this.addConsequences(new Consequence("Action id receiver", "producer")
		{

			@Override
			public Step run() throws Exception
			{
				Boolean result = future.get();

				setSucess(result.booleanValue());
				setDone(true);

				return this;
			}

		});
	}

	protected void addEpilogues()
	{
		this.addEpilogue(new Epilogue("Epilogue")
		{
			public Step run() throws Exception
			{

				try
				{
					infoConsumer.unsubscribe(NetAction.DestinationType.TOPIC, subscriptionName);

					Sleep.time(1000);
					infoConsumer.close();

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
