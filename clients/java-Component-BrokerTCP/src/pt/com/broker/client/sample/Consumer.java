package pt.com.broker.client.sample;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.caudexorigo.cli.CliFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

/**
 * Simple consumer sample. Behavior is determined by command line arguments.
 * 
 */

public class Consumer implements BrokerListener
{

	private static final Logger log = LoggerFactory.getLogger(Consumer.class);
	private final AtomicInteger counter = new AtomicInteger(0);

	private String host;
	private int port;
	private DestinationType dtype;
	private String dname;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		Consumer consumer = new Consumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();

		BrokerClient bk = new BrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer");

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		bk.addAsyncConsumer(subscribe, consumer);

		System.out.println("listening...");
	}

	@Override
	public boolean isAutoAck()
	{
		return dtype != DestinationType.TOPIC;
	}

	long time = 0;
	volatile long count = 0;
	@Override
	public void onMessage(NetNotification notification) 
	{
		log.info(String.format(" [%s] %s -> Message destination: %s Received Message Length: %s (%s)", new Date(System.currentTimeMillis()), counter.incrementAndGet(), notification.getDestination(), notification.getMessage().getPayload().length, new String(notification.getMessage().getPayload())));
//		if( ((++count) % 1000 ) == 0)
//		{
//			log.info(String.format(" [%s] %s -> Message destination: %s Received Message Length: %s (%s)", new Date(System.currentTimeMillis()), count, notification.getDestination(), notification.getMessage().getPayload().length, new String(notification.getMessage().getPayload())));
//			System.out.println("Time: " + (System.nanoTime() - time));
//			time = System.nanoTime();
//		}
		
		
	}

}
