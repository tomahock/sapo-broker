package pt.com.broker.functests.positive;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.conf.ConfigurationInfo;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;

public class DeferredDeliveryQueueTest extends BrokerTest
{
	private static final String DEFERRED_DELIVERY_HEADER = "DEFERRED_DELIVERY";

	private BrokerClient brokerClient = null;

	private long publishDate = System.currentTimeMillis();

	private long deferredDeliveryTime = 1000;

	private long expectedDeliveryTime = publishDate + deferredDeliveryTime;

	private String queueName = String.format("/queue/%s", RandomStringUtils.randomAlphanumeric(10));
	final Object sync = new Object();

	public DeferredDeliveryQueueTest()
	{
		super("Deferred Delivery Queue Test");

		this.setTimeout(3000);

		try
		{
			this.brokerClient = new BrokerClient(ConfigurationInfo.getParameter("agent1-host"), BrokerTest.getAgent1Port(), "tcp://mycompany.com/test", getEncodingProtocolType());
		}
		catch (Throwable e)
		{
			setFailure(e);
		}
	}

	@Override
	protected void build() throws Throwable
	{
		this.addPrerequisite(new Prerequisite(this.getName() + "prerequisite")
		{
			@Override
			public Step run()
			{
				try
				{
					System.out.println("DeferredDeliveryQueueTest.build().new Prerequisite() {...}.run()");

					brokerClient.addAsyncConsumer(new NetSubscribe(queueName, DestinationType.QUEUE), new BrokerListener()
					{
						@Override
						public void onMessage(NetNotification message)
						{
							long now = System.currentTimeMillis();

							long acceptableInterval = 500;

							setDone(true);
							if ((now > expectedDeliveryTime + acceptableInterval) || (now < expectedDeliveryTime - acceptableInterval))
							{
								setSucess(false);
								setReasonForFailure("Message received in a not acceptable interval. Dif: " + (now - expectedDeliveryTime));
							}
							else
							{
								setSucess(true);
							}
							synchronized (sync)
							{
								sync.notifyAll();
							}
						}

						@Override
						public boolean isAutoAck()
						{
							return true;
						}
					});

				}
				catch (Throwable e)
				{
					setFailure(e);
				}

				return this;

			}
		});

		this.setAction(new Action(this.getName() + " - Action", "Producer")
		{

			@Override
			public Step run() throws Exception
			{
				System.out.println("DeferredDeliveryQueueTest.build().new Action() {...}.run()");

				NetBrokerMessage message = new NetBrokerMessage("payload");

				message.addHeader(DEFERRED_DELIVERY_HEADER, deferredDeliveryTime + "");

				brokerClient.enqueueMessage(message, queueName);

				synchronized (sync)
				{
					try
					{
						sync.wait(5000);
						setSucess(true);
					}
					catch (Exception e)
					{
						setFailure(e);
					}
				}

				return this;
			}
		});

		// this.addConsequences(new Consequence(this.getName(), "Consequence")
		// {
		// @Override
		// public Step run() throws Exception
		// {
		// System.out.println("DeferredDeliveryQueueTest.build().new Consequence() {...}.run()");
		// //brokerClient.close();
		//
		//
		// return this;
		// }
		// });
	}

	@Override
	public boolean skipTest()
	{
		return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON);
	}

}
