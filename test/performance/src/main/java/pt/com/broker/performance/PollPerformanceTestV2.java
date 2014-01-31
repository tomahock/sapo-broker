package pt.com.broker.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.Shutdown;

public class PollPerformanceTestV2
{
	private static String hostname = "localhost";
	private static int port = 3323;

	private static final int NUMBER_OF_CONSUMERS = 32;

	private static final int MESSAGES_PRODUCED = 1500;

	private static final String QUEUE_NAME = "/queue/performance";

	public static void main(String[] args) throws Throwable
	{

		CountDownLatch cdp = new CountDownLatch(1);

		QueueProducer qp = new QueueProducer(MESSAGES_PRODUCED, hostname, port, QUEUE_NAME, cdp);

		qp.run();

		cdp.await();

		CountDownLatch cdc = new CountDownLatch(NUMBER_OF_CONSUMERS);
		ExecutorService exec = Executors.newFixedThreadPool(NUMBER_OF_CONSUMERS);
		AtomicInteger counter = new AtomicInteger(MESSAGES_PRODUCED);

		double start, stop, elapsed;

		start = System.nanoTime();

		for (int i = 0; i < NUMBER_OF_CONSUMERS; i++)
		{
			// SyncConsumerClient sc = new SyncConsumerClient(counter, "Client#" + i, hostname, port, QUEUE_NAME, cdc);
			SyncConsumerClientV2 sc = new SyncConsumerClientV2(counter, "Client#" + i, hostname, port, QUEUE_NAME, cdc);

			exec.execute(sc);
		}

		cdc.await();

		stop = System.nanoTime();

		elapsed = (stop - start) / (1000 * 1000 * 1000);

		System.out.println("Test Ended!");
		System.out.println("Messages produced: " + MESSAGES_PRODUCED);
		System.out.println("Number of consumer: " + NUMBER_OF_CONSUMERS);
		System.out.println("Time (in seconds): " + elapsed);

		Shutdown.now();
	}
}
