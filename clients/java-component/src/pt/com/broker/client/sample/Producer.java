package pt.com.broker.client.sample;

import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.Shutdown;
import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetProtocolType;

/**
 * Simple producer sample. Behavior is determined by command line arguments.
 * 
 */
public class Producer
{
	private static final Logger log = LoggerFactory.getLogger(Producer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;
	private long delay;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
		Producer producer = new Producer();

		producer.host = cargs.getHost();
		producer.port = cargs.getPort();
		producer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		producer.dname = cargs.getDestination();
		producer.delay = cargs.getDelay();

		NetProtocolType protocolType = NetProtocolType.valueOf(cargs.getProtocolType());

		BrokerClient bk = new BrokerClient(producer.host, producer.port, "tcp://mycompany.com/mypublisher", protocolType);

		log.info("Start sending string of " + cargs.getMessageLength() + " random alphanumeric characters in 1 seconds to " + producer.dname + "...");

		producer.sendLoop(bk, cargs.getMessageLength());
		bk.close();
		Shutdown.now();
	}

	private void sendLoop(BrokerClient bk, int messageLength) throws Throwable
	{

		// final String msg = RandomStringUtils.randomAlphanumeric(messageLength);
		// NetBrokerMessage brokerMessage = new NetBrokerMessage(msg);
		for (int i = 0; i < 50000; ++i)
		{
			final String msg = i + " - " + RandomStringUtils.randomAlphanumeric(messageLength);
			NetBrokerMessage brokerMessage = new NetBrokerMessage(msg);

			// brokerMessage.setExpiration(System.currentTimeMillis() + 1000);

			if (dtype == DestinationType.QUEUE)
			{
				bk.enqueueMessage(brokerMessage, dname);
			}
			else
			{
				bk.publishMessage(brokerMessage, dname);
			}

			log.info(String.format("%s -> Send Message: %s", counter.incrementAndGet(), msg));

			Sleep.time(delay);
		}
	}
}
