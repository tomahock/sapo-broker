package pt.com.broker.client.nio.ignore;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetAction.DestinationType;


/**
 * Tests what happens when a client is disconnected from the agent and the main
 * thread continues to publish messages. Is the memory exausted? Are the messages published?
 * What happens?
 * */
public class TestUnawareClient implements Runnable {
	
	static final Logger log = LoggerFactory.getLogger(TestUnawareClient.class);
	
	private static final String BROKER_HOST = "127.0.0.1";
	private static final Integer BROKER_PORT = 3323;
	private static final String BROKER_TOPIC = "/sapo/broker/dev/unawareclient";
	
	
	private Integer nMessages;
	private UnawareBrokerClient unawareBrokerClient = new UnawareBrokerClient();
	private BrokerClient bClient;
	
	private AtomicInteger totalPublishedMessages = new AtomicInteger();
	private AtomicInteger totalConsumedMessages = new AtomicInteger();
	
	public TestUnawareClient(Integer nMessages){
		this.nMessages = nMessages;
		bClient = new BrokerClient(BROKER_HOST, BROKER_PORT, NetProtocolType.PROTOCOL_BUFFER);
		bClient.connect();
	}
	
	class UnawareBrokerClient {
		
		private BrokerClient bClient;
		
		public UnawareBrokerClient() {
			try{
				bClient = new BrokerClient("127.0.0.1", 3323, NetProtocolType.PROTOCOL_BUFFER);
				bClient.connect();
				bClient.subscribe(BROKER_TOPIC, DestinationType.TOPIC, new NotificationListenerAdapter() {
					
					@Override
					public boolean onMessage(NetNotification notification, HostInfo host) {
						try {
							log.info("Received a message: {}", new String(notification.getMessage().getPayload(), "UTF-8"));
							totalConsumedMessages.incrementAndGet();
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					}
				});
			} catch(Exception e){
				log.error("Unexpected exception caught.", e);
				System.exit(-1);
			}
		}
		
	}

	@Override
	public void run() {
		for(int i = 0; i < nMessages; i++){
			bClient.publish("Test broker Message", BROKER_TOPIC, DestinationType.TOPIC);
			totalPublishedMessages.incrementAndGet();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		int nMessages = 1000;
		TestUnawareClient t = new TestUnawareClient(nMessages);
		Thread test = new Thread(t);
		test.start();
		test.join();
		log.debug("N messages: {}", nMessages);
		log.debug("Total published messages: {}", t.totalPublishedMessages.get());
		log.debug("Total consumed messages: {}", t.totalConsumedMessages.get());
	}

}
