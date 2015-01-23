package pt.com.broker.client.nio.ignore;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetAction.DestinationType;

public class MultiThreadSync {
	
	static final Logger log = LoggerFactory.getLogger(MultiThreadSync.class);
	
	private static final String BROKER_TOPIC = "/sapo/broker/dev/multithreading/test";
	private static Integer messagesReceived = 0;
	
	static class Producer implements Runnable {
		
		private int nMessages;
		private BrokerClient bClient;
		
		public Producer(int nMessages){
			this.nMessages = nMessages;
			bClient = new BrokerClient("127.0.0.1", 3323, NetProtocolType.PROTOCOL_BUFFER);
			bClient.connect();
		}

		@Override
		public void run() {
			for(int i = 0; i < nMessages; i++){
				bClient.publish("Test broker Message", BROKER_TOPIC, DestinationType.TOPIC);
			}
		}
		
	}
	
	@Test
	public void testNonAthomicVariables() throws InterruptedException{
		BrokerClient consumer = new BrokerClient("127.0.0.1", 3323, NetProtocolType.PROTOCOL_BUFFER);
		consumer.connect();
		consumer.subscribe(BROKER_TOPIC, DestinationType.TOPIC, new NotificationListenerAdapter() {
			
			@Override
			public boolean onMessage(NetNotification notification, HostInfo host) {
				try {
					log.info("Thread attending the request: {}", Thread.currentThread().getName());
//					log.info("Received a message: {}", new String(notification.getMessage().getPayload(), "UTF-8"));
					messagesReceived += 1;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		});
		Thread p1 = new Thread(new Producer(1000));
		Thread p2 = new Thread(new Producer(1000));
		Thread p3 = new Thread(new Producer(1000));
		Thread p4 = new Thread(new Producer(1000));
		Thread p5 = new Thread(new Producer(1000));
		Thread p6 = new Thread(new Producer(1000));
		Thread p7 = new Thread(new Producer(1000));
		Thread p8 = new Thread(new Producer(1000));
		p1.start();
		p2.start();
		p3.start();
		p4.start();
		p5.start();
		p6.start();
		p7.start();
		p8.start();
		p1.join();
		p2.join();
		p3.join();
		p4.join();
		p5.join();
		p6.join();
		p7.join();
		p8.join();
		Thread.sleep(10000);
		log.debug("Total received messages: {}", messagesReceived);
	}

}
