package pt.com.broker.client.sample;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.CliArgs;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;

/**
 * Consumer sample using SSL.
 * 
 */
public class SslConsumer implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(SslConsumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;
	private String keystoreLocation;
	private String keystorePassword;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		SslConsumer consumer = new SslConsumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();
		consumer.keystoreLocation = cargs.getKeystoreLocation();
		consumer.keystorePassword = cargs.getKeystorePassword();

		SslBrokerClient bk = new SslBrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer", NetProtocolType.PROTOCOL_BUFFER, consumer.keystoreLocation, consumer.keystorePassword);

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer);

		System.out.println("listening...");
	}

	@Override
	public boolean isAutoAck()
	{
		if (dtype == DestinationType.TOPIC)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		System.out.printf("===========================     [%s]#%s   =================================%n", new Date(), counter.incrementAndGet());
		System.out.printf("Destination: '%s'%n", notification.getDestination());
		System.out.printf("Subscription: '%s'%n", notification.getSubscription());
		System.out.printf("Payload: '%s'%n", new String(notification.getMessage().getPayload()));

	}

}