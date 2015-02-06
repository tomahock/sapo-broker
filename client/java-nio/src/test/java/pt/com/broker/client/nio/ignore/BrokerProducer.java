package pt.com.broker.client.nio.ignore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetAction.DestinationType;

public class BrokerProducer {
	
	public static final TimeUnit DEFAULT_PRODUCER_TIME_UNIT 		= TimeUnit.SECONDS;
	public static final long DEFAULT_PRODUCER_PERIOD 				= 1; //One second
	
	private BrokerClient bClient;
	private DestinationType destinationType;
	private String destination;
	
	public BrokerProducer(String host, int port, DestinationType destinationType, String destination){
		this.destinationType = destinationType;
		this.destination = destination;
		this.bClient = new BrokerClient(host, port);
		bClient.connect();
	}
	
	public void startProducing(final String brokerMessage){
		startProducing(brokerMessage, DEFAULT_PRODUCER_PERIOD, DEFAULT_PRODUCER_TIME_UNIT);
	}
	
	public void startProducing(final String brokerMessage, long period, TimeUnit unit){
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				bClient.publish(brokerMessage, destination, destinationType);
			}
			
		}, 0, period, unit);
	}

}
