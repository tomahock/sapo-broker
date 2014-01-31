package pt.com.broker.performance;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.cli.CliFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;

public class ConsumerApp implements BrokerListener
{

	// private static final Logger log = LoggerFactory.getLogger(ConsumerApp.class);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	private CountDownLatch countDown = new CountDownLatch(1);

	volatile int sentMessages;
	AtomicLong startTime = new AtomicLong(0);
	volatile long stopTime;
	AtomicInteger counter = new AtomicInteger(0);

	public static void main(String[] args) throws Throwable
	{
		final TestCliArgs cargs = CliFactory.parseArguments(TestCliArgs.class, args);

		ConsumerApp consumer = new ConsumerApp();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();

		BrokerClient bk = new BrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer");

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer);

		System.out.println("Waiting...");

		consumer.countDown.await();

		bk.close();

		System.out.println(String.format("Received messages: %s, Sent messages: %s", consumer.counter.get() + "", consumer.sentMessages + ""));
		System.out.println(String.format("Time: %s (ns)", (consumer.stopTime - consumer.startTime.get()) + ""));

		double nano2second = (1000 * 1000 * 1000); // nanos
		double time = (double) consumer.stopTime - consumer.startTime.get();
		double totalNrOfMessagesSent = (double) consumer.sentMessages;
		double timePerMsg = ((((double) time)) / totalNrOfMessagesSent) / nano2second;
		double messagesPerSecond = 1 / timePerMsg;
		String result = String.format("--------> Messages: %s.Time: %s (s). Time per message: %s (s). Messages per second: %s\n", (int) totalNrOfMessagesSent, time / nano2second, timePerMsg, messagesPerSecond);
		System.out.println(result);
	}

	@Override
	public boolean isAutoAck()
	{
		return dtype != DestinationType.TOPIC;
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		if (!startTime.compareAndSet(0, System.nanoTime()))
		{
			if (notification.getMessage().getPayload()[0] == ProducerApp.STOP_MESSAGE)
			{
				stopTime = System.nanoTime();

				byte[] payload = notification.getMessage().getPayload();
				byte[] serializedCount = new byte[payload.length - 1];

				System.arraycopy(payload, 1, serializedCount, 0, serializedCount.length);

				String sentMessagesStr = new String(serializedCount);

				sentMessages = Integer.parseInt(sentMessagesStr);
				countDown.countDown();
			}
			else
			{
				int localCounter = counter.incrementAndGet();
				if ((localCounter % 100) == 0)
				{
					System.out.println("Messages received: " + localCounter);
				}
			}
		}
		else
		{
			counter.incrementAndGet();
			System.out.println("First message received!");
		}
	}
}
