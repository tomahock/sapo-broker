package pt.com.broker.functests;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

public class ResubscriptionTest {
	
	private static final String BROKER_HOST = "127.0.0.1";
	private static final Integer BROKER_PORT = 3323;
	private static final String BROKER_TOPIC = "/topic/test";
	private static final String BROKER_QUEUE = "/queue/test";
	
	public static class TopicProducer implements Runnable {
		
		private BrokerClient bClient;
		
		public TopicProducer(){
			bClient = new BrokerClient(BROKER_HOST, BROKER_PORT, NetProtocolType.PROTOCOL_BUFFER);
			bClient.connect();
			new Thread(this).start();
		}

		@Override
		public void run() {
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

	public static class TopicConsumer{
		
		private BrokerClient bClient;
		
		static final Logger log = LoggerFactory.getLogger(TopicConsumer.class);
		
		public TopicConsumer(){
			bClient = new BrokerClient(BROKER_HOST, BROKER_PORT, NetProtocolType.PROTOCOL_BUFFER);
			bClient.connect();
			try {
				consume();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void consume() throws InterruptedException{
			bClient.subscribe(BROKER_TOPIC, DestinationType.TOPIC, new BrokerListener() {
				
				@Override
				public void deliverMessage(NetMessage message, HostInfo host)
						throws Throwable {
					// TODO Auto-generated method stub
					log.debug("Received a new message");
				}
			});
		}
		
	}
	
	public static class QueueProducer implements Runnable {
		
		private BrokerClient bClient;
		
		public QueueProducer() {
			bClient = new BrokerClient(BROKER_HOST, BROKER_PORT, NetProtocolType.PROTOCOL_BUFFER);
			bClient.connect();
			new Thread(this).start();
		}

		@Override
		public void run() {
			for(;;){
				try{
					bClient.publish("Test broker Message", BROKER_QUEUE, DestinationType.QUEUE);
					Thread.sleep(1000);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static class QueueConsumer {
		
		private static final Logger log = LoggerFactory.getLogger(QueueConsumer.class);
		
		private BrokerClient bClient;
		
		public QueueConsumer() {
			bClient = new BrokerClient(BROKER_HOST, BROKER_PORT, NetProtocolType.PROTOCOL_BUFFER);
			bClient.connect();
			try {
				consume();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void consume() throws InterruptedException{
			bClient.subscribe(BROKER_QUEUE, DestinationType.QUEUE, new NotificationListenerAdapter() {
				
				@Override
				public boolean onMessage(NetNotification notification, HostInfo host) {
					try {
						log.debug("Received a message: {}", new String(notification.getMessage().getPayload(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return true;
				}
			});
		}
		
	}
	
	public static void main(String[] args){
//		TopicProducer p = new TopicProducer();
//		TopicConsumer c = new TopicConsumer();
		QueueProducer qp = new QueueProducer();
		QueueConsumer qc = new QueueConsumer();
	}

}