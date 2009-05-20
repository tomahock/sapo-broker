package pt.com.broker.client.sample;

import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.auth.AuthInfo;
import pt.com.broker.auth.CredentialsProviderFactory;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.SslBrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class AuthenticatedConsumer implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(AuthenticatedConsumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	private String stsLocation;
	private String stsUsername;
	private String stsPassword;

	private String keystoreLocation;
	private String keystorePassword;

	private static void initSTSParams(String stsLocation)
	{
		SapoSTSAuthenticationParamsProvider.Parameters parameters = new SapoSTSAuthenticationParamsProvider.Parameters(stsLocation);
		SapoSTSAuthenticationParamsProvider.setSTSParameters(parameters);
	}

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
		consumer.keystoreLocation = cargs.getKeystoreLocation();
		consumer.keystorePassword = cargs.getKeystorePassword();

		// Provider initialization
		initSTSParams(consumer.stsLocation);
		//CredentialsProviderFactory.addProvider("SapoSTS", new SapoSTSAuthenticationCredentialsProvider());
		// Provider initialized

		SslBrokerClient bk = new SslBrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer", NetProtocolType.PROTOCOL_BUFFER, consumer.keystoreLocation, consumer.keystorePassword.toCharArray());

		AuthInfo clientAuthInfo = new AuthInfo(consumer.stsUsername, consumer.stsPassword);
		clientAuthInfo.setUserAuthenticationType("SapoSTS");

		AuthInfo stsClientCredentials = CredentialsProviderFactory.getProvider("SapoSTS").getCredentials(clientAuthInfo);

		bk.setAuthenticationCredentials(stsClientCredentials);
		try
		{
			bk.authenticateClient();
		}
		catch (Throwable t)
		{
			System.out.println("Unable to authenticate client...");
			System.out.println(t);
		}

		// TODO: create callback object with time out
		System.out.println("giving time to authenticate (3s)...");
		Thread.sleep(3000);
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
