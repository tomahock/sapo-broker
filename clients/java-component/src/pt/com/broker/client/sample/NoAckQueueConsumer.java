package pt.com.broker.client.sample;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetSubscribe;

public class NoAckQueueConsumer implements BrokerListener
{
	private static final Logger log = LoggerFactory.getLogger(Consumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;
	private long waitTime;
	private NetProtocolType protocol;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		NoAckQueueConsumer consumer = new NoAckQueueConsumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();
		consumer.waitTime = cargs.getDelay();
		consumer.protocol = NetProtocolType.valueOf(cargs.getProtocolType());

		BrokerClient bk = new BrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer", consumer.protocol);

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);
		subscribe.addHeader("ACK_REQUIRED", "false");

		bk.addAsyncConsumer(subscribe, consumer);

		System.out.println("listening...");
	}

	@Override
	public boolean isAutoAck()
	{
		return false;
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		System.out.printf("===========================     [%s]#%s   =================================%n", new Date(), counter.incrementAndGet());
		System.out.printf("Destination: '%s'%n", notification.getDestination());
		System.out.printf("Subscription: '%s'%n", notification.getSubscription());
		System.out.printf("Payload: '%s'%n", new String(notification.getMessage().getPayload()));

		Map<String, String> headers = notification.getHeaders();
		if (headers != null)
		{
			System.out.printf("Headers: %n");
			for (String headerName : headers.keySet())
			{
				System.out.printf(" '%s' - '%s'%n", headerName, headers.get(headerName));
			}
		}

		Sleep.time(waitTime);
	}
}
