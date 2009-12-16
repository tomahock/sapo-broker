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
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class DistConsumerApp implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(DistConsumerApp.class);

	private String host;
	private int port;
	private String dname;

	private String actorName;

	private BrokerClient brokerClient;

	public static void main(String[] args) throws Throwable
	{
		final DistTestCliArgs cargs = CliFactory.parseArguments(DistTestCliArgs.class, args);

		DistConsumerApp consumer = new DistConsumerApp();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();

		consumer.actorName = cargs.getActorName();

		consumer.dname = TestManager.TEST_MANAGEMENT_ACTION + consumer.actorName;

		consumer.brokerClient = new BrokerClient(consumer.host, consumer.port);

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, DestinationType.QUEUE);

		consumer.brokerClient.addAsyncConsumer(subscribe, consumer);

		System.out.println(String.format("Consumer '%s' running...", consumer.actorName));

		while (true)
		{
			Sleep.time(5000);
		}

	}

	private void performTest(DistTestParams testParams)
	{
		try
		{
			//BrokerClient bk = new BrokerClient(host, port);
			
			BrokerClient bk = new BrokerClient(testParams.getClientInfo().getAgentHost(), testParams.getClientInfo().getPort());

			final DestinationType destType = testParams.getDestinationType();

			NetSubscribe subscribe = new NetSubscribe(testParams.getDestination(), destType);

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
						startTime.compareAndSet(0, System.nanoTime());
						
						if (notification.getMessage().getPayload()[0] == ProducerApp.REGULAR_MESSAGE)
						{
							counter.incrementAndGet();
						}
						else
						{
							stopTime.compareAndSet(0, System.nanoTime());
							countDown.countDown();
						}
					}

					@Override
					public boolean isAutoAck()
					{
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

				startTime.set(System.nanoTime());
				
				do
				{
					NetNotification notification = bk.poll(testParams.getDestination());
					if (notification.getMessage().getPayload()[0] == ProducerApp.REGULAR_MESSAGE)
					{
						counter.incrementAndGet();
					}
					else
					{
						stopTime.compareAndSet(0, System.nanoTime());
						stop = true;
					}
					bk.acknowledge(notification);
				}
				while (!stop);
			}

			TestResult testResult = new TestResult(ActorType.Consumer, actorName, testParams.getTestName(), counter.get(), stopTime.get() - startTime.get());
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
