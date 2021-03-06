package pt.com.broker.functests.positive;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.caudexorigo.text.RandomStringUtils;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.functests.Action;
import pt.com.broker.functests.Prerequisite;
import pt.com.broker.functests.Step;
import pt.com.broker.functests.helpers.BrokerTest;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;

public class DeferredDeliveryQueueTest extends BrokerTest
{

	private final CountDownLatch latch = new CountDownLatch(1);

	private static final String DEFERRED_DELIVERY_HEADER = "DEFERRED_DELIVERY";

	private BrokerClient brokerClient = null;

	private long publishDate = System.currentTimeMillis();

	private long deferredDeliveryTime = 1000;

	private long expectedDeliveryTime = publishDate + deferredDeliveryTime;

	private String queueName = String.format("/queue/%s", RandomStringUtils.randomAlphanumeric(10));
	final Object sync = new Object();

	public DeferredDeliveryQueueTest(NetProtocolType protocolType)
	{

		super(protocolType);

		setName("Deferred Delivery Queue Test");

		this.setTimeout(3000);

		try
		{
			this.brokerClient = new BrokerClient(getAgent1Hostname(), getAgent1Port(), getEncodingProtocolType());
			this.brokerClient.connect();
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

					brokerClient.subscribe(new NetSubscribe(queueName, DestinationType.QUEUE), new NotificationListenerAdapter()
					{

						@Override
						public boolean onMessage(NetNotification message, HostInfo host)
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

							return true;
						}

					}).get();

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

				Future f = brokerClient.publish(message, queueName, DestinationType.QUEUE);

				f.get();

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

	/*
	 * @Override public boolean skipTest() { return (getEncodingProtocolType() == NetProtocolType.SOAP) || (getEncodingProtocolType() == NetProtocolType.SOAP_v0) || (getEncodingProtocolType() == NetProtocolType.JSON); }
	 */

	public static Collection getProtocolTypes()
	{
		return Arrays.asList(new Object[][] {
				{ NetProtocolType.PROTOCOL_BUFFER },
				{ NetProtocolType.THRIFT },
		});
	}

}
