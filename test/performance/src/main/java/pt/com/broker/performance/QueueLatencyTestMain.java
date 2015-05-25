package pt.com.broker.performance;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.concurrent.Sleep;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.Headers;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class QueueLatencyTestMain
{
	/**
	 * Tests queue latency by measuring the time spent from between Enqueue and Received.
	 */

	// Test Main
	public static void main(String[] args) throws Throwable
	{

		runTest();
		runTest();

	}

	private static void runTest() throws Throwable
	{
		// Test parameters
		final int NUMBER_OF_MESSAGES = 1000;
		final String BROKER_HOST = "127.0.0.1";
		final int BROKER_PORT = 3323;

		final String DESTINATION_NAME = "/Queue/LatencyTest";
		final DestinationType DESTINATION_TYPE = DestinationType.QUEUE;

		// Broker
		// final NetBrokerMessage message = new NetBrokerMessage("payload");

		// Test data
		AtomicInteger testCount = new AtomicInteger(0);
		final AtomicInteger receivedMessages = new AtomicInteger(0);
		final AtomicLong minLatency = new AtomicLong(Long.MAX_VALUE);
		final AtomicLong maxLatency = new AtomicLong(0L);
		final AtomicLong totalLantecy = new AtomicLong(0L);

		BrokerClient brokerClientConsumer = new BrokerClient(BROKER_HOST, BROKER_PORT);

		final CountDownLatch latch = new CountDownLatch(1);

		NetSubscribe netSubscribe = new NetSubscribe(DESTINATION_NAME, DESTINATION_TYPE);
		netSubscribe.addHeader(Headers.ACK_REQUIRED, "false");

		brokerClientConsumer.addAsyncConsumer(netSubscribe, new BrokerListener()
		{

			@Override
			public void onMessage(NetNotification message)
			{
				long now = System.nanoTime();
				long send_ts = Long.parseLong(new String(message.getMessage().getPayload()));
				long elapsed = now - send_ts;

				if ((receivedMessages.get() % 100) == 0)
				{
					System.out.println("Received: " + receivedMessages.get());
				}

				if (elapsed < minLatency.get())
				{
					minLatency.set(elapsed);
				}
				if (elapsed > maxLatency.get())
				{
					maxLatency.set(elapsed);
				}
				totalLantecy.addAndGet(elapsed);

				if (receivedMessages.incrementAndGet() == NUMBER_OF_MESSAGES)
				{
					latch.countDown();
				}
			}

			@Override
			public boolean isAutoAck()
			{
				return false;
			}
		});

		System.out.println();
		System.out.println("[Starting test]");

		long global_start = System.currentTimeMillis();
		Random rnd = new Random();

		BrokerClient brokerClientProducer = new BrokerClient(BROKER_HOST, BROKER_PORT);
		do
		{
			if ((testCount.get() % 100) == 0)
			{
				System.out.println("Sent: " + testCount.get());
			}

			// send message
			NetBrokerMessage message = new NetBrokerMessage(Long.toString(System.nanoTime()));

			if (DESTINATION_TYPE.equals(DestinationType.TOPIC))
			{
				brokerClientProducer.publishMessage(message, DESTINATION_NAME);
			}
			else
			{
				brokerClientProducer.enqueueMessage(message, DESTINATION_NAME);
			}

			long rnd_slepp = Math.abs(rnd.nextLong());
			Sleep.time(rnd_slepp % 20);

		}
		while (testCount.incrementAndGet() != NUMBER_OF_MESSAGES);

		latch.await();
		long global_end = System.currentTimeMillis();

		long global_elapsed = global_end - global_start;

		brokerClientConsumer.close();
		brokerClientProducer.close();

		// process and show test data
		System.out.println("Test results:");
		System.out.println(String.format("Min latency:		%s ms", minLatency.doubleValue() / 1000000.0));
		System.out.println(String.format("Max latency:		%s ms", maxLatency.doubleValue() / 1000000.0));
		System.out.printf("Average latency:	%s ms%n", (totalLantecy.doubleValue() / (double) NUMBER_OF_MESSAGES) / 1000000.0);

		// System.out.println(String.format("Min latency:		%s ms", minLatency.doubleValue()));
		// System.out.println(String.format("Max latency:		%s ms", maxLatency.doubleValue()));
		// System.out.printf("Average latency:	%s ms%n", (totalLantecy.doubleValue() / (double) NUMBER_OF_MESSAGES));

		System.out.printf("Test Duration: %sms%n", global_elapsed);
	}
}