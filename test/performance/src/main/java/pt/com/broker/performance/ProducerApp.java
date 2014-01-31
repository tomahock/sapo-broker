package pt.com.broker.performance;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;

/**
 * Simple producer sample. Behavior is determined by command line arguments.
 * 
 */
public class ProducerApp
{
	public static final byte STOP_MESSAGE = (byte) 0;
	public static final byte REGULAR_MESSAGE = (byte) 1;

	private static final ExecutorService executer = Executors.newFixedThreadPool(16);

	private static final Logger log = LoggerFactory.getLogger(ProducerApp.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private AtomicInteger threadsWorking;

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	public static void main(String[] args) throws Throwable
	{
		final TestCliArgs cargs = CliFactory.parseArguments(TestCliArgs.class, args);
		final ProducerApp producer = new ProducerApp();

		producer.host = cargs.getHost();
		producer.port = cargs.getPort();
		producer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		producer.dname = cargs.getDestination();

		final BrokerClient bk = new BrokerClient(producer.host, producer.port, "tcp://mycompany.com/mypublisher");

		Thread.sleep(200);

		int producingThreads = cargs.getProducingThreads();

		log.info("Start sending " + cargs.getNumberOfMessages() + " strings of " + cargs.getMessageLength() + " random alphanumeric characters in 200 milliseconds to " + producer.dname + " using " + producingThreads + " threads.");

		final int messagesPerThread = cargs.getNumberOfMessages() / producingThreads;

		ArrayList<Callable<Integer>> producers = new ArrayList<Callable<Integer>>(producingThreads);

		for (int i = 0; i != producingThreads; ++i)
		{
			producers.add(new Callable<Integer>()
			{
				@Override
				public Integer call() throws Exception
				{
					try
					{
						producer.sendLoop(bk, cargs.getMessageLength(), messagesPerThread, cargs.getNumberOfMessages());
					}
					catch (Throwable e)
					{
						e.printStackTrace();
					}
					return null;
				}
			});
		}

		producer.threadsWorking = new AtomicInteger(producingThreads);

		executer.invokeAll(producers);

		// producer.sendLoop(bk, cargs.getMessageLength(), cargs.getNumberOfMessages());

		System.out.println("Done!");

		System.exit(0);
	}

	private void sendLoop(BrokerClient bk, int messageLength, int nrOfMessages, int totalMessages) throws Throwable
	{
		final String regularMsgContent = RandomStringUtils.randomAlphanumeric(messageLength - 1);
		final String stopMsgContent = totalMessages + "";

		byte[] regularMessage = getMessage(REGULAR_MESSAGE, regularMsgContent);
		byte[] stopMessage = getMessage(STOP_MESSAGE, stopMsgContent);

		NetBrokerMessage brokerMessage = new NetBrokerMessage(regularMessage);
		NetBrokerMessage stopBrokerMessage = new NetBrokerMessage(stopMessage);

		for (int i = 0; i != nrOfMessages; ++i)
		{

			if (dtype == DestinationType.QUEUE)
			{
				bk.enqueueMessage(brokerMessage, dname);
			}
			else
			{
				bk.publishMessage(brokerMessage, dname);
			}

			// log.info(String.format("%s -> Send Message: %s", counter.incrementAndGet(), msg));

			// Sleep.time(3);
		}

		if (threadsWorking.decrementAndGet() == 0)
		{

			System.out.println("Sending stop messages");

			for (int i = 0; i != 10; ++i)
			{

				if (dtype == DestinationType.QUEUE)
				{
					bk.enqueueMessage(stopBrokerMessage, dname);
				}
				else
				{
					bk.publishMessage(stopBrokerMessage, dname);
				}

				// log.info(String.format("%s -> Send Message: %s", counter.incrementAndGet(), msg));

				// Sleep.time(3);
			}

			bk.close();
		}
	}

	private byte[] getMessage(byte headerByte, String messageContent)
	{
		byte[] serializedContent = new byte[0];
		try
		{
			serializedContent = messageContent.getBytes("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			// this will never happen
		}
		byte[] serializedMessage = new byte[serializedContent.length + 1];

		serializedMessage[0] = headerByte;
		System.arraycopy(serializedContent, 0, serializedMessage, 1, serializedContent.length);

		return serializedMessage;
	}

}