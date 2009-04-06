package pt.com.broker.client.sample;

import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.common.security.ClientAuthInfo;
import pt.com.common.security.authentication.AuthenticationCredentialsProviderFactory;
import pt.com.security.authentication.sapoSts.SapoSTSAuthenticationParamsProvider;
import pt.com.types.NetNotification;
import pt.com.types.NetProtocolType;
import pt.com.types.NetSubscribe;
import pt.com.types.NetAction.DestinationType;

public class AuthenticatedConsumer implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(AuthenticatedConsumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	private int sslPort;
	private int useSsl;

	private String stsLocation;
	private String stsUsername;
	private String stsPassword;

	private String keystoreLocation;
	private String keystorePassword;

	private static void initSTSParams(AuthenticatedConsumer params)
	{
		SapoSTSAuthenticationParamsProvider.Parameters parameters = new SapoSTSAuthenticationParamsProvider.Parameters(params.stsLocation);
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

		consumer.sslPort = cargs.getSslPort();
		consumer.useSsl = cargs.useSsl();
		consumer.stsLocation = cargs.getSTSLocation();
		consumer.stsUsername = cargs.getSTSUsername();
		consumer.stsPassword = cargs.getSTSPassword();
		consumer.keystoreLocation = cargs.getKeystoreLocation();
		consumer.keystorePassword = cargs.getKeystorePassword();

		initSTSParams(consumer);

		BrokerClient bk = null;
		if (consumer.useSsl == 0)
		{
			bk = new BrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer");
		}
		else
		{
			bk = new BrokerClient(consumer.host, consumer.port, consumer.sslPort, "tcp://mycompany.com/mysniffer", NetProtocolType.PROTOCOL_BUFFER, consumer.keystoreLocation, consumer.keystorePassword.toCharArray());
		}

		ClientAuthInfo clientAuthInfo = new ClientAuthInfo(consumer.stsUsername, consumer.stsPassword);

		bk.setAuthenticationCredentials(AuthenticationCredentialsProviderFactory.getProvider("SapoSTS"), clientAuthInfo);
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
		System.out.println("giving time to authenticate (5s)...");
		Thread.sleep(5000);

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer, (cargs.useSsl() != 0));

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
