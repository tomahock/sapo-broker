package pt.com.broker.client.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.HostInfo;
import pt.com.broker.types.NetAction;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetPublish;
import pt.com.broker.types.NetAction.ActionType;
import pt.com.broker.types.NetAction.DestinationType;

/**
 * Simple UDP producer sample.
 * 
 */

public class UDPProducer
{
	private static final Logger log = LoggerFactory.getLogger(UDPProducer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private int udpPort;
	private DestinationType dtype;
	private String dname;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
		UDPProducer producer = new UDPProducer();

		producer.host = cargs.getHost();
		producer.port = cargs.getPort();
		producer.udpPort = cargs.getUdpPort();
		producer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		producer.dname = cargs.getDestination();

		List<HostInfo> hosts = new ArrayList<HostInfo>(1);
		hosts.add(new HostInfo(producer.host, producer.port, producer.udpPort));

		BrokerClient bk = new BrokerClient(hosts);

		log.info("Start sending a string of " + cargs.getMessageLength() + " random alphanumeric characters in 2 seconds...");

		Thread.sleep(2000);

		producer.sendLoop(bk, cargs.getMessageLength());
	}

	private void sendLoop(BrokerClient bk, int messageLength) throws Throwable
	{
		for (int i = 0; i < 100000; i++)
		{
			final String msg = RandomStringUtils.randomAlphanumeric(messageLength);

			NetBrokerMessage brokerMessage = new NetBrokerMessage(msg.getBytes("UTF-8"));

			NetPublish publishMsg = new NetPublish(dname, dtype, brokerMessage);

			bk.publishMessageOverUdp(publishMsg);

			log.info(String.format("%s -> Send Message: %s", counter.incrementAndGet(), msg));

			Sleep.time(500);
		}
	}
}
