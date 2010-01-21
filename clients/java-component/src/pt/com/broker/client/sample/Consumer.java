package pt.com.broker.client.sample;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.caudexorigo.cli.CliFactory;
import org.caudexorigo.concurrent.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.CliArgs;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetMessage;
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
	
	private BrokerClient bk;

	public static void main(String[] args) throws Throwable
	{
		final CliArgs cargs = CliFactory.parseArguments(CliArgs.class, args);

		Consumer consumer = new Consumer();

		consumer.host = cargs.getHost();
		consumer.port = cargs.getPort();
		consumer.dtype = DestinationType.valueOf(cargs.getDestinationType());
		consumer.dname = cargs.getDestination();

		consumer.bk = new BrokerClient(consumer.host, consumer.port, "tcp://mycompany.com/mysniffer");

		NetSubscribe subscribe = new NetSubscribe(consumer.dname, consumer.dtype);

		consumer.bk.addAsyncConsumer(subscribe, consumer);

		System.out.println("listening...");
	}

	@Override
	public boolean isAutoAck()
	{
		return dtype != DestinationType.TOPIC;
	}

	
	volatile long latestReport = System.nanoTime();
	AtomicLong count = new AtomicLong(0);
	
	AtomicLong total = new AtomicLong(0);
	

	volatile long refTime = System.currentTimeMillis();
	volatile long sleepTime = 5;
	
	
	@Override
	public void onMessage(NetNotification notification)
	{
		System.out.printf("===========================     [%s]#%s   =================================%n", new Date(), counter.incrementAndGet());
		System.out.printf("Destination: '%s'%n", notification.getDestination());
		System.out.printf("Subscription: '%s'%n", notification.getSubscription());
		System.out.printf("DestinationType: '%s'%n", notification.getDestinationType());
		System.out.printf("Payload: '%s'%n", new String(notification.getMessage().getPayload()));
		if(notification.getNetMessage() != null)
		{
			System.out.printf("-------- Headers ------------\n");
			NetMessage netMessage = notification.getNetMessage();
			Map<String, String> headers = netMessage.getHeaders();
			if(headers != null)
			{
				for(String key : headers.keySet() )
				{
					System.out.printf("%s\t%s\n", key, headers.get(key));
				}
			}
		}
		
		
		
//		count.incrementAndGet();
//		long incrementAndGet = total.incrementAndGet();
//		
//		long now = System.nanoTime();
//		long diff = (now - latestReport); 
//		if( diff >  (500 * 1000 * 1000 * 1000) )
//		{
//		
//			System.out.printf("%s - %s - %s%n", new Date(), count.get() , incrementAndGet);
//			
//			count.set(0);
//			latestReport = now;
//		}
//		
//		if(System.currentTimeMillis() > (refTime + 45 * 1000 ))
//		{
//			if( sleepTime != 5)
//			{
//				sleepTime = 5;
//				System.out.println("Fast");
//			}
//			else
//			{
//				sleepTime = 2500;
//				System.out.println("Slow");
//			}
//			refTime = System.currentTimeMillis();
//		}
//		
//		Sleep.time(sleepTime);
	}

}
