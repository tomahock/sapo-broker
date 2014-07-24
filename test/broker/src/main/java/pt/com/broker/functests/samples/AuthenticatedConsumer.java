package pt.com.broker.functests.samples;

import org.caudexorigo.cli.CliFactory;
import org.apache.commons.lang3.StringUtils;
import pt.com.broker.auth.CredentialsProvider;
import pt.com.broker.auth.saposts.SapoSTSProvider;
import pt.com.broker.client.nio.SslBrokerClient;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumer sample where an authenticate user is used. This samples uses SapoSTS.
 * 
 */
public class AuthenticatedConsumer extends NotificationListenerAdapter
{
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	private String stsLocation;
	private String stsUsername;
	private String stsPassword;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		AuthenticatedConsumer consumer = new AuthenticatedConsumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();

		consumer.stsLocation = cargs.getSTSLocation();
		consumer.stsUsername = cargs.getUsername();
		consumer.stsPassword = cargs.getUserPassword();

		SslBrokerClient bk = new SslBrokerClient(NetProtocolType.PROTOCOL_BUFFER);
        bk.addServer(consumer.host, consumer.port);

		CredentialsProvider cp = StringUtils.isBlank(consumer.stsLocation) ? new SapoSTSProvider(consumer.stsUsername, consumer.stsPassword) : new SapoSTSProvider(consumer.stsUsername, consumer.stsPassword, consumer.stsLocation);

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

		System.out.println("subscribing");
		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.subscribe(subscribe, consumer);

		System.out.println("listening...");
	}


    @Override
    public boolean onMessage(NetNotification notification, HostInfo host) {

        System.out.printf("===========================     [%s]#%s   =================================%n", new Date(), counter.incrementAndGet());
        System.out.printf("Destination: '%s'%n", notification.getDestination());
        System.out.printf("Subscription: '%s'%n", notification.getSubscription());
        System.out.printf("Payload: '%s'%n", new String(notification.getMessage().getPayload()));

        if (dtype == DestinationType.TOPIC)
        {
            return false;
        }
        else
        {
            return true;
        }
    }


}