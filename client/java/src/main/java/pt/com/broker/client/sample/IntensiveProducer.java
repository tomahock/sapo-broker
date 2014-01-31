package pt.com.broker.client.sample;

import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.Shutdown;
import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;

public class IntensiveProducer
{
	private static final Logger log = LoggerFactory.getLogger(Producer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
		IntensiveProducer producer = new IntensiveProducer();

		producer.host = cargs.getHost();
		producer.port = cargs.getPort();
		producer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		producer.dname = cargs.getDestination();

		BrokerClient bk = new BrokerClient(producer.host, producer.port, "tcp://mycompany.com/mypublisher");

		Thread.sleep(1000);

		producer.sendLoop(bk, cargs.getMessageLength());

		Shutdown.now();
	}

	private void sendLoop(BrokerClient bk, int messageLength) throws Throwable
	{
		final String msg = RandomStringUtils.randomAlphanumeric(messageLength);

		NetBrokerMessage brokerMessage = new NetBrokerMessage(msg.getBytes("UTF-8"));

		long start, stop;
		start = System.currentTimeMillis();

		for (int j = 0; j < 1000000; j++)
		{

			if (dtype == DestinationType.QUEUE)
			{
				bk.enqueueMessage(brokerMessage, dname);
			}
			else
			{
				bk.publishMessage(brokerMessage, dname);
			}

			if (counter.incrementAndGet() % 5000 == 0)
			{
				log.info(String.format("%s -> Send Message: %s", counter.get(), msg));
			}
		}
		stop = System.currentTimeMillis();

		double duration = ((double) (stop - start)) / ((double) 1000);

		log.info(String.format("Total time: %.2f sec.", duration));
	}
}
