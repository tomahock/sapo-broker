package pt.com.broker.client.nio.ignore;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.events.LogConnectionEventListener;
import pt.com.broker.client.nio.events.NotificationListenerAdapter;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetAction.DestinationType;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetProtocolType;

//TODO: This class does not bellong here. Change this behaviour into a test
public class ResubscriptionTest {
	
	private static final String AGENT_1_BROKER_HOST = "broker.bk.sapo.pt";
	private static final Integer AGENT_1_BROKER_PORT = 3323;
	private static final String AGENT_2_BROKER_HOST = "localhost";
	private static final Integer AGENT_2_BROKER_PORT = 3423;
	private static final String BROKER_TOPIC = "/sapo/broker/dev/topic/test";
	private static final String BROKER_QUEUE = "/sapo/broker/dev/queue/test";
	private static final String BROKER_SNIFF_TOPIC = "/.*hp.*";
	
	public static class TopicProducer implements Runnable {
		
		private BrokerClient bClient;
		
		public TopicProducer(String brokerHost, Integer brokerPort){
			bClient = new BrokerClient(brokerHost, brokerPort, NetProtocolType.PROTOCOL_BUFFER);
			bClient.addConnectionEventListener(new LogConnectionEventListener());
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
		
		private static final Logger log = LoggerFactory.getLogger(TopicConsumer.class);
		
		private BrokerClient bClient;
		private String topic;
		
		public TopicConsumer(String brokerHost, Integer brokerPort, String topic){
			bClient = new BrokerClient(brokerHost, brokerPort, NetProtocolType.PROTOCOL_BUFFER);
			this.topic = topic;
			bClient.addConnectionEventListener(new LogConnectionEventListener());
			bClient.connect();
			try {
				consume();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void consume() throws InterruptedException{
			bClient.subscribe(topic, DestinationType.TOPIC, new BrokerListener() {
				
				@Override
				public void deliverMessage(NetMessage message, HostInfo host)
						throws Throwable {
					log.debug("Received a new message for topic: {}", message.getAction().getNotificationMessage().getDestination());
//					log.debug("Message contents: {}", new String(message.getAction().getNotificationMessage().getMessage().getPayload(), "UTF-8"));
				}
			});
		}
		
	}
	
	public static class QueueProducer implements Runnable {
		
		private BrokerClient bClient;
		
		public QueueProducer(String brokerHost, Integer brokerPort) {
			bClient = new BrokerClient(brokerHost, brokerPort, NetProtocolType.PROTOCOL_BUFFER);
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
		
		public QueueConsumer(String brokerHost, Integer brokerPort) {
			bClient = new BrokerClient(brokerHost, brokerPort, NetProtocolType.PROTOCOL_BUFFER);
			bClient.addConnectionEventListener(new LogConnectionEventListener());
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
//		TopicProducer p = new TopicProducer(AGENT_1_BROKER_HOST, AGENT_1_BROKER_PORT);
//		TopicConsumer c = new TopicConsumer(AGENT_1_BROKER_HOST, AGENT_1_BROKER_PORT);
//		QueueProducer qp = new QueueProducer();
//		QueueConsumer qc = new QueueConsumer();
		TopicConsumer sniffer = new TopicConsumer(AGENT_1_BROKER_HOST, AGENT_1_BROKER_PORT, BROKER_SNIFF_TOPIC);
	}

}