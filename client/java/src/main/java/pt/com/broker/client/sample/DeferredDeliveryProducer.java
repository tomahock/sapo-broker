package pt.com.broker.client.sample;

import org.caudexorigo.Shutdown;
import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.text.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.types.Headers;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetProtocolType;

public class DeferredDeliveryProducer
{
	private static final Logger log = LoggerFactory.getLogger(Producer.class);

	private String host;
	private int port;
	private String dname;

	private long deferredDelivery;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);
		DeferredDeliveryProducer producer = new DeferredDeliveryProducer();

		producer.host = cargs.getHost();
		producer.port = cargs.getPort();
		producer.dname = cargs.getDestination();
		producer.deferredDelivery = (cargs.getDelay() == 0) ? 1000 : cargs.getDelay();

		NetProtocolType protocolType = NetProtocolType.valueOf(cargs.getProtocolType());

		BrokerClient bk = new BrokerClient(producer.host, producer.port, "tcp://mycompany.com/mypublisher", protocolType);

		log.info("Sending message. Size: " + cargs.getMessageLength() + ". This message should be received in " + producer.deferredDelivery + "milliseconds.");

		final String msg = RandomStringUtils.randomAlphanumeric(cargs.getMessageLength());

		NetBrokerMessage brokerMessage = new NetBrokerMessage(msg);

		// Specify the delivery interval (in milliseconds)
		brokerMessage.addHeader(Headers.DEFERRED_DELIVERY, "" + producer.deferredDelivery);

		bk.enqueueMessage(brokerMessage, producer.dname);

		bk.close();
		Shutdown.now();
	}

}
