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

/**
 * Simple consumer sample. Behavior is determined by command line arguments.
 * 
 */

public class Consumer implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(Consumer.class);
	private static final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;
	private long waitTime;
	private NetProtocolType protocolType;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		Consumer consumer = new Consumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();
		consumer.waitTime = cargs.getDelay();
		consumer.protocolType = NetProtocolType.valueOf(cargs.getProtocolType());

		BrokerClient bk = new BrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer", consumer.protocolType);
		/*
		 * bk.setNumberOfTries(0);
		 * 
		 * bk.setErrorListener(new BrokerErrorListenter() {
		 * 
		 * @Override public void onFault(NetFault fault) { System.out.println("## FAULT: " + fault.getMessage());
		 * 
		 * }
		 * 
		 * @Override public void onError(Throwable throwable) { System.out.println("## ERROR: [" + throwable.getClass().getCanonicalName() + "] Message: " + throwable.getMessage()); } }); System.out.println("Hello. Trying error");
		 */

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer);

		System.out.println("listening...");

	}

	@Override
	public boolean isAutoAck()
	{
		return dtype != DestinationType.TOPIC;
	}

	@Override
	public void onMessage(NetNotification notification)
	{
		System.out.printf("===========================     [%s]#%s   =================================%n", new Date(), counter.incrementAndGet());
		System.out.printf("Destination: '%s'%n", notification.getDestination());
		System.out.printf("Subscription: '%s'%n", notification.getSubscription());
		System.out.printf("Payload: '%s'%n", new String(notification.getMessage().getPayload()));
		Map<String, String> headers = notification.getHeaders();
		if ((headers != null) && (headers.size() != 0))
		{
			System.out.printf("Headers:");
			for (String key : headers.keySet())
			{
				System.out.printf("  %s ->  %s%n", key, headers.get(key));
			}
		}

		if (waitTime > 0)
		{
			Sleep.time(waitTime);
		}
	}
}
