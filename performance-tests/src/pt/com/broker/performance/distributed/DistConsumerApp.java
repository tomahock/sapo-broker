package pt.com.broker.performance.distributed;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.performance.ProducerApp;
import pt.com.broker.performance.distributed.TestResult.ActorType;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class DistConsumerApp implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(DistConsumerApp.class);

	private String host;
	private int port;
	private String dname;

	private String actorName;

	private BrokerClient brokerClient;

	public DistConsumerApp(String host, int port, String actorName) throws Throwable
	{
		this.host = host;
		this.port = port;
		this.actorName = actorName;

		dname = TestManager.TEST_MANAGEMENT_ACTION + actorName;
		brokerClient = new BrokerClient(host, port);

		NetSubscribe subscribe = new NetSubscribe(dname, DestinationType.QUEUE);

		brokerClient.addAsyncConsumer(subscribe, this);

		System.out.println(String.format("Consumer '%s' running...", actorName));

	}

	public static void main(String[] args) throws Throwable
	{
		final DistTestCliArgs cargs = CliFactory.parseArguments(DistTestCliArgs.class, args);

		DistConsumerApp consumer = new DistConsumerApp(cargs.getHost(), cargs.getPort(), cargs.getActorName());

		while (true)
		{
			Sleep.time(5000);
		}
	}

	private void performTest(final DistTestParams testParams)
	{
		try
		{
			// BrokerClient bk = new BrokerClient(host, port);

			System.out.println("########## Consumer using: " + testParams.getEncoding());

			BrokerClient bk = new BrokerClient(testParams.getClientInfo().getAgentHost(), testParams.getClientInfo().getPort(), "ConsumerActor", testParams.getEncoding());

			final DestinationType destType = testParams.getDestinationType();

			NetSubscribe subscribe = new NetSubscribe(testParams.getDestination(), destType);

			if (testParams.isNoAckConsumer())
			{
				subscribe.addHeader("ACK_REQUIRED", "false");
			}

			final AtomicInteger counter = new AtomicInteger(0);
			final AtomicLong startTime = new AtomicLong(0);
			final AtomicLong stopTime = new AtomicLong(0);

			System.out.println(actorName + " starting new test: " + testParams.getTestName());

			if (!testParams.isSyncConsumer())
			{
				final CountDownLatch countDown = new CountDownLatch(1);
				bk.addAsyncConsumer(subscribe, new BrokerListener()
				{
					@Override
					public void onMessage(NetNotification notification)
					{
						startTime.compareAndSet(0, System.currentTimeMillis());

						if (notification.getMessage().getPayload()[0] == ProducerApp.REGULAR_MESSAGE)
						{
							counter.incrementAndGet();
						}
						else
						{
							stopTime.compareAndSet(0, System.currentTimeMillis());
							countDown.countDown();
						}
					}

					@Override
					public boolean isAutoAck()
					{
						if (testParams.isNoAckConsumer())
						{
							return false;
						}
						return destType != DestinationType.TOPIC;
					}
				});
				countDown.await();
				bk.unsubscribe(subscribe.getDestinationType(), subscribe.getDestination());
			}
			else
			{
				boolean stop = false;

				System.out.println(actorName + " Sync consumer");

				startTime.set(System.currentTimeMillis());

				do
				{
					NetNotification notification = bk.poll(testParams.getDestination());
					if (notification.getMessage().getPayload()[0] == ProducerApp.REGULAR_MESSAGE)
					{
						counter.incrementAndGet();
					}
					else
					{
						stopTime.compareAndSet(0, System.currentTimeMillis());
						stop = true;
					}
					bk.acknowledge(notification);
				}
				while (!stop);
			}

			Sleep.time(500);

			bk.close();

			TestResult testResult = new TestResult(ActorType.Consumer, actorName, testParams.getTestName(), counter.get(), startTime.get(), stopTime.get());
			byte[] data = testResult.serialize();

			NetBrokerMessage netBrokerMessage = new NetBrokerMessage(data);

			String destination = TestManager.TEST_MANAGEMENT_RESULT;

			System.out.println(actorName + " Sending results");

			brokerClient.enqueueMessage(netBrokerMessage, destination);

			System.out.println(actorName + " Test ended with sucess");

		}
		catch (Throwable t)
		{
			log.error("Test failed", t);
		}
	}

	@Override
	public boolean isAutoAck()
	{
		return false;
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		byte[] testParams = notification.getMessage().getPayload();

		DistTestParams distTestParams = DistTestParams.deserialize(testParams);
		try
		{
			brokerClient.acknowledge(notification);
		}
		catch (Throwable t)
		{
			log.error("Failed to ack test message", t);
		}

		performTest(distTestParams);
	}
}
