package pt.com.broker.auth.saposts.samples;

import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.caudexorigo.text.RandomStringUtils;
import org.caudexorigo.text.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetProtocolType;

/**
 * Consumer sample where an authenticate user is used. This samples uses SapoSTS.
 */

public class AuthenticatedProducer
{

	private static final Logger log = LoggerFactory.getLogger(AuthenticatedProducer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;
	private long delay;

	private String stsLocation;
	private String stsUsername;
	private String stsPassword;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		AuthenticatedProducer producer = new AuthenticatedProducer();

		producer.host = cargs.getHost();
		producer.port = cargs.getPort();
		producer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		producer.dname = cargs.getDestination();
		producer.delay = cargs.getDelay();

		producer.stsLocation = cargs.getSTSLocation();
		producer.stsUsername = cargs.getUsername();
		producer.stsPassword = cargs.getUserPassword();

		SslBrokerClient bk = new SslBrokerClient(producer.host, producer.port, "tcp://mycompany.com/mysniffer", NetProtocolType.PROTOCOL_BUFFER);

		CredentialsProvider cp = StringUtils.isBlank(producer.stsLocation) ? new SapoSTSProvider(producer.stsUsername, producer.stsPassword) : new SapoSTSProvider(producer.stsUsername, producer.stsPassword, producer.stsLocation);

		bk.setCredentialsProvider(cp);

		try
		{
			if (!bk.authenticateClient())
			{
				System.out.println("Authentication failed");
				return;
			}
		}
		catch (Throwable t)
		{
			System.out.println("Unable to authenticate client...");
			System.out.println(t);
			return;
		}

		producer.sendLoop(bk, cargs.getMessageLength());

	}

	private void sendLoop(SslBrokerClient bk, int messageLength) throws Throwable
	{
		// final String msg = RandomStringUtils.randomAlphanumeric(messageLength);
		// NetBrokerMessage brokerMessage = new NetBrokerMessage(msg);
		for (int i = 0; i < 50000; ++i)
		{
			final String msg = i + " - " + RandomStringUtils.randomAlphanumeric(messageLength);
			NetBrokerMessage brokerMessage = new NetBrokerMessage(msg);

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

			System.out.print('.');
		}
	}
}
