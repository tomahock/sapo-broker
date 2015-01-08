package pt.com.broker.client.nio.ignore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetAction.DestinationType;

/**
 * Tests one client connecting to multiple agents at the same time.
 * */
public class MultipleAgentsTest {
	
	private static final Logger log = LoggerFactory.getLogger(MultipleAgentsTest.class);
	
	private static final HostInfo[] agents = new HostInfo[2];
	
	static {
		agents[0] = new HostInfo("localhost", 3323);
		agents[1] = new HostInfo("localhost", 3423);
	}

	public static void main(String[] args) throws InterruptedException {
		BrokerClient bClient = new BrokerClient(NetProtocolType.PROTOCOL_BUFFER);
		for(int i = 0; i < agents.length; i++){
			bClient.addServer(agents[i]);
		}
		bClient.connect();
		log.debug("*****************************************Connect method passed!*****************************************");
		bClient.subscribe("/test/topic", DestinationType.TOPIC, new BrokerListener() {
			
			@Override
			public void deliverMessage(NetMessage message, HostInfo host)
					throws Throwable {
				// TODO Auto-generated method stub
				log.debug("Received a new message");
			}
		});
	}

}
