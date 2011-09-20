package pt.com.broker.client.sample;

import java.util.Date;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.types.NetNotification;

/**
 * Queue consumer sample using Poll method - synchronous consumer.
 * 
 */
public class QueueSyncConsumer
{
	private static final Logger log = LoggerFactory.getLogger(QueueSyncConsumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private String dname;
	private long waitTime;
	private long timeout;
	private long reserveTime;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		QueueSyncConsumer qsconsumer = new QueueSyncConsumer();

		qsconsumer.host = cargs.getHost();
		qsconsumer.port = cargs.getPort();
		qsconsumer.dname = cargs.getDestination();
		qsconsumer.waitTime = cargs.getDelay();
		qsconsumer.timeout = cargs.getPollTimeout();
		qsconsumer.reserveTime = cargs.getReserveTime();

		BrokerClient bk = new BrokerClient(qsconsumer.host, qsconsumer.port, "tcp://mycompany.com/mysniffer");

		qsconsumer.receiveLoop(bk);
	}

	private void receiveLoop(BrokerClient bk) throws Throwable
	{
		System.out.println("reserveTime= " + reserveTime);
		System.out.println("timeout= " + timeout);

		while (true)
		{
			NetNotification notification = null;
			if (timeout > 0)
			{
				log.info("Send Poll request with timeout: {}ms", timeout);

				try
				{
					if (reserveTime == -1)
					{
						notification = bk.poll(dname, timeout, null);
					}
					else
					{
						notification = bk.poll(dname, timeout, reserveTime, null);
					}
				}
				catch (TimeoutException te)
				{
					log.info("No message received in the specified timeout: {}ms", timeout);
					notification = null;
				}

				if (notification == null)
				{
					if (waitTime > 0)
					{
						Sleep.time(waitTime);
					}
					continue;
				}
			}
			else
			{
				log.info("Send Poll request without timeout");
				// notification = bk.poll(dname);
				if (reserveTime == -1)
				{
					notification = bk.poll(dname, timeout, null);
				}
				else
				{
					notification = bk.poll(dname, timeout, reserveTime, null);
				}
			}

			if (notification != null)
			{
				System.out.printf("===========================     [%s]#%s   =================================%n", new Date(), counter.incrementAndGet());
				System.out.printf("Destination: '%s'%n", notification.getDestination());
				System.out.printf("Subscription: '%s'%n", notification.getSubscription());
				System.out.printf("Payload: '%s'%n", new String(notification.getMessage().getPayload()));

				if (waitTime > 0)
				{
					log.info("Sleep for {}ms", waitTime);
					Sleep.time(waitTime);
				}
				bk.acknowledge(notification);
			}
		}
	}
}