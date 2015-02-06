package pt.com.broker.client.nio.ignore;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.types.NetAction.DestinationType;

public class BrokerTestSubscribe {
	
	private static final String BROKER_HOST = "localhost";
	private static final int BROKER_PORT = 3323;
	private static final DestinationType DESTINATION_TYPE = DestinationType.TOPIC;
	private static final String DESTINATION = "/dev/test/subscriptions";

	public static void main(String[] args) throws Exception {
		BrokerConsumer consumer = new BrokerConsumer(BROKER_HOST, BROKER_PORT, DESTINATION_TYPE, DESTINATION);
		BrokerProducer producer = new BrokerProducer(BROKER_HOST, BROKER_PORT, DESTINATION_TYPE, DESTINATION);
		producer.startProducing("Test Subscribe!");
	}

}
