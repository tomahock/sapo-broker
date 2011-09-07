package pt.com.broker.auth.jdbc.samples;

import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.jdbc.JdbcProvider;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;

/**
 * Consumer sample where an authenticate user is used. This samples uses database based authentication.
 * 
 */
public class AuthenticatedDBConsumer implements BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(AuthenticatedDBConsumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	private String username;
	private String password;

	private String keystoreLocation;
	private String keystorePassword;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		AuthenticatedDBConsumer consumer = new AuthenticatedDBConsumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();
		consumer.keystoreLocation = cargs.getKeystoreLocation();
		consumer.keystorePassword = cargs.getKeystorePassword();

		consumer.username = cargs.getUsername();
		consumer.password = cargs.getUserPassword();


		SslBrokerClient bk = new SslBrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer", NetProtocolType.PROTOCOL_BUFFER, consumer.keystoreLocation, consumer.keystorePassword);

		CredentialsProvider cp = new JdbcProvider(consumer.username, consumer.password);
		
		bk.setCredentialsProvider(cp);
		try
		{
			if (!bk.authenticateClient())
			{
				return;
			}
		}
		catch (Throwable t)
		{
			log.error("Failed to authenticate client", t);
			return;
		}

		System.out.println("subscribing");
		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer, null);

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
		log.info(String.format("%s -> Received Message Length: %s (%s)", counter.incrementAndGet(), notification.getMessage().getPayload().length, new String(notification.getMessage().getPayload())));
	}
}
