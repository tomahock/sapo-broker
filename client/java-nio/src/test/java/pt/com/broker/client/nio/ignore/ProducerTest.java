package pt.com.broker.client.nio.ignore;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetAction.DestinationType;

public class ProducerTest {
	
	private static final String BROKER_TOPIC = "/test/topic";

	public static void main(String[] args) {
		BrokerClient bClient = new BrokerClient("10.0.2.2", 3323);
		bClient.connect();
		for(;;){
			try{
				bClient.publish("Test broker Message", BROKER_TOPIC, DestinationType.TOPIC);
				//Sleep for 1 second
				Thread.sleep(1000);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}

}
