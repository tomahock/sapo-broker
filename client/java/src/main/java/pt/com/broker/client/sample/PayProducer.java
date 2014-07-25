package pt.com.broker.client.sample;

import org.caudexorigo.Shutdown;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.com.broker.client.BrokerClient;
import pt.com.broker.types.NetBrokerMessage;

import java.util.concurrent.atomic.AtomicInteger;

public class PayProducer
{
	private static final Logger log = LoggerFactory.getLogger(PayProducer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private final String dname;
	private final long waitTime;
	private BrokerClient bk;
	private int msgCount;

	public PayProducer(String host, int port, String dname, long waitTime, int msgCount)
	{
		this.dname = dname;
		this.waitTime = waitTime;
		this.msgCount = msgCount;

		try
		{
			bk = new BrokerClient(host, port);
		}
		catch (Throwable t)
		{
			Shutdown.now(t);
		}
	}

	public void sendLoop(int messageLength)
	{
		log.info("Start sending string of {} random alphanumeric characters in 1 seconds to '{}' ...", messageLength, dname);

		for (int i = 0; i < msgCount; ++i)
		{
			final String msg = i + " - " + RandomStringUtils.randomAlphanumeric(messageLength);
			NetBrokerMessage brokerMessage = new NetBrokerMessage(msg);

			bk.enqueueMessage(brokerMessage, dname);
			log.info(String.format("%s -> Send Message: %s", counter.incrementAndGet(), msg));

			Sleep.time(waitTime);
		}

		bk.close();
	}
}