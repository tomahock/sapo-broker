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
			BrokerClient bk = new BrokerClient(host, port);

			final DestinationType destType = testParams.getDestinationType();

			NetSubscribe subscribe = new NetSubscribe(testParams.getDestination(), destType);

			final CountDownLatch countDown = new CountDownLatch(1);
			final AtomicInteger counter = new AtomicInteger(0);
			final AtomicLong startTime = new AtomicLong(0);
			final AtomicLong stopTime = new AtomicLong(0);

			final DestinationType destinationType = testParams.getDestinationType();
			final int numberOfMessages = testParams.getNumberOfMessagesToReceive();

			System.out.println(actorName + " starting new test: " + testParams.getTestName());
			System.out.println("Number of messages: " + numberOfMessages);
			
			bk.addAsyncConsumer(subscribe, new BrokerListener()
			{

				final AtomicInteger counter2 = new AtomicInteger(0);
				
				@Override
				public void onMessage(NetNotification notification)
				{
					if((counter2.incrementAndGet() % 50) == 0)
					{
						System.out.println( String.format("%s on message: %s", actorName,counter2.get()));
					}
					
					if (destinationType == DestinationType.TOPIC)
					{
						if (notification.getMessage().getPayload()[0] == ProducerApp.REGULAR_MESSAGE)
						{
							counter.incrementAndGet();
						}
						else
						{
							stopTime.set(System.nanoTime());

							countDown.countDown();
						}
					}
					else
					{
						if (counter.incrementAndGet() == numberOfMessages)
						{
							stopTime.set(System.nanoTime());
							countDown.countDown();
						}
						if(counter.get() > numberOfMessages)
						{
							log.error(String.format("%s - counter.get() > numberOfMessages (%s)", actorName,counter.get()));
						}
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

			TestResult testResult = new TestResult(ActorType.Consumer, actorName, testParams.getTestName(), counter.get(), stopTime.get() - startTime.get());
			byte[] data = testResult.serialize();

			NetBrokerMessage netBrokerMessage = new NetBrokerMessage(data);

			String destination = TestManager.TEST_MANAGEMENT_RESULT;

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
