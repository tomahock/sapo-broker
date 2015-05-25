package pt.com.broker.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

import com.google.common.util.concurrent.RateLimiter;

/**
 * Tests sending messages to broker at a high speed rate defined on the options and checks if all sent messages are delivered as they are supposed to.
 * */
public class TestHighRateVq
{

	static final Logger log = LoggerFactory.getLogger(TestHighRateVq.class);

	private static final String BROKER_HOST = "localhost";
	private static final int BROKER_PORT = 3323;
	private static final int TOPIC_1_RATE_LIMIT = 690; // Sent messages per second
	private static final int TOPIC_2_RATE_LIMIT = 300;
	private static final String BROKER_TOPIC_1 = "/sapo/dev/broker/test_vq";
	private static final String BROKER_TOPIC_2 = "/sapo/dev/broker/test_vq_2";
	private static final String BROKER_VQ_1 = String.format("vq@%s", BROKER_TOPIC_1);
	private static final String BROKER_VQ_2 = String.format("vq2@%s", BROKER_TOPIC_2);

	private static AtomicInteger sentMessagesTopic1 = new AtomicInteger(0);
	private static AtomicInteger receivedMessagesVq1 = new AtomicInteger(0);
	private static AtomicLong startProducingTsTopic1 = new AtomicLong(0);
	private static AtomicLong starReceivingtTsVq1 = new AtomicLong(0);

	private static AtomicInteger sentMessagesTopic2 = new AtomicInteger(0);
	private static AtomicInteger receivedMessagesVq2 = new AtomicInteger(0);
	private static AtomicLong startProducingTsTopic2 = new AtomicLong(0);
	private static AtomicLong starReceivingtTsVq2 = new AtomicLong(0);

	public static synchronized void timeTakingTask()
	{
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Throwable
	{
		// Consumer
		BrokerClient consumer = new BrokerClient(BROKER_HOST, BROKER_PORT);
		// First queue consumer
		consumer.addAsyncConsumer(new NetSubscribe(BROKER_VQ_1, DestinationType.VIRTUAL_QUEUE), new BrokerListener()
		{

			@Override
			public void onMessage(NetNotification message)
			{
				if (starReceivingtTsVq1.get() == 0L)
				{
					starReceivingtTsVq1.set(System.currentTimeMillis());
				}
				receivedMessagesVq1.incrementAndGet();
				// Block the client for a period of time
				if (receivedMessagesVq1.get() % 15000 == 0)
				{
					timeTakingTask();
				}
			}

			@Override
			public boolean isAutoAck()
			{
				return true;
			}
		});
		// Second queue consumer
		consumer.addAsyncConsumer(new NetSubscribe(BROKER_VQ_2, DestinationType.VIRTUAL_QUEUE), new BrokerListener()
		{

			@Override
			public void onMessage(NetNotification message)
			{
				if (starReceivingtTsVq2.get() == 0L)
				{
					starReceivingtTsVq2.set(System.currentTimeMillis());
				}
				receivedMessagesVq2.incrementAndGet();
			}

			@Override
			public boolean isAutoAck()
			{
				return true;
			}
		});

		final BrokerClientProducer producer1 = new BrokerClientProducer(BROKER_HOST, BROKER_PORT);
		// Producer1 Thread
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				startProducingTsTopic1.set(System.currentTimeMillis());
				RateLimiter rt = RateLimiter.create(TOPIC_1_RATE_LIMIT);
				while (true)
				{
					rt.acquire();
					producer1.produceMessage("Dummy Payload", BROKER_TOPIC_1);
					sentMessagesTopic1.incrementAndGet();
				}
			}
		}).start();

		final BrokerClientProducer producer2 = new BrokerClientProducer(BROKER_HOST, BROKER_PORT);
		// Producer1 Thread
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				startProducingTsTopic2.set(System.currentTimeMillis());
				RateLimiter rt = RateLimiter.create(TOPIC_2_RATE_LIMIT);
				while (true)
				{
					rt.acquire();
					producer2.produceMessage("Dummy Payload", BROKER_TOPIC_2);
					sentMessagesTopic2.incrementAndGet();
				}
			}
		}).start();

		// Stats scheduller
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable()
		{

			@Override
			public void run()
			{
				long currentTs = System.currentTimeMillis();
				long messagesSentSoFarTopic1 = sentMessagesTopic1.get();
				long messagesReceivedSoFarVq1 = receivedMessagesVq1.get();
				long producerElapsedTimeTopic1 = (currentTs - startProducingTsTopic1.get()) / 1000;
				long consumerElapsedTimeVq1 = (currentTs - starReceivingtTsVq1.get()) / 1000;
				double producingRateTopic1 = producerElapsedTimeTopic1 != 0 ? (messagesSentSoFarTopic1 / producerElapsedTimeTopic1) : 0.0;
				double consumingRateVq1 = consumerElapsedTimeVq1 != 0 ? (messagesReceivedSoFarVq1 / consumerElapsedTimeVq1) : 0.0;
				// log.debug("Total Produced Messages so far Topic 1: {}", messagesSentSoFarTopic1);
				// log.debug("Total Consumed Messages so far VQ1: {}", messagesReceivedSoFarVq1);
				// log.debug("Producer Rate Topic1: {} msg/s", producingRateTopic1);
				// log.debug("Consumer Rate VQ1: {} msg/s", consumingRateVq1);
			}

		}, 1000, 1000, TimeUnit.MILLISECONDS);
	}

}
