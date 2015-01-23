package pt.com.broker.functests.positive;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.client.nio.BrokerClient;
import pt.com.broker.client.nio.events.BrokerListener;
import pt.com.broker.client.nio.server.HostInfo;
import pt.com.broker.types.NetMessage;
import pt.com.broker.types.NetProtocolType;
import pt.com.broker.types.NetAction.DestinationType;

public class MultipleAgentsTests {
	
	protected static final String BROKER_AGENT_1_HOST = "localhost";
	protected static final Integer BROKER_AGENT_1_PORT = 3323;
	protected static final String BROKER_AGENT_2_HOST = "localhost";
	protected static final Integer BROKER_AGENT_2_PORT = 3423;
	protected static final String BROKER_TOPIC = "/sapo/broker/test/topic";
	protected static final String VIRTUALQUEUE_BROKER_TOPIC = "vq@" + BROKER_TOPIC;
	protected static final String BROKER_QUEUE = "/sapo/broker/test/queue";
	protected static final Integer PRODUCER_SLEEP_TIME = 100;
	protected static final Integer CONSUMER_WAIT_TIME_MILIS = 10000;
	
	protected static final Logger log = LoggerFactory.getLogger(MultipleAgentsTests.class);
	
	private static AtomicInteger producedMessages = new AtomicInteger();
	private static AtomicInteger consumedMessages = new AtomicInteger();

	private static class Producer implements Runnable {
		
		private BrokerClient bClient;
		private int nMessages;
		private DestinationType destinationType;
		private String topic;
		private CountDownLatch producerFinished;
		
		public Producer(String brokerHost, Integer brokerPort, int nMessages, DestinationType destinationType, String topic, CountDownLatch producerFinished){
			bClient = new BrokerClient(brokerHost, brokerPort, NetProtocolType.PROTOCOL_BUFFER);
			this.nMessages = nMessages;
			this.destinationType = destinationType;
			this.topic = topic;
			this.producerFinished = producerFinished;
			bClient.connect();
		}

		@Override
		public void run() {
			try{
				for(int i = 0; i < nMessages; i++){
					bClient.publish("Test broker Message", topic, destinationType);
					producedMessages.incrementAndGet();
					try {
						Thread.sleep(PRODUCER_SLEEP_TIME);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} finally {
				log.debug("Producer finished!");
				producerFinished.countDown();
			}
		}
		
	}
	
	private static class Consumer implements Runnable {
		
		private BrokerClient bClient;
		private int nMessages;
		private DestinationType destinationType;
		private String topic;
		private CountDownLatch consumerReady;
		private CountDownLatch consumerFinished;
		
		public Consumer(String brokerHost, Integer brokerPort, int nMessages, DestinationType destinationType, String topic, CountDownLatch consumerReady, CountDownLatch consumerFinished){
			bClient = new BrokerClient(brokerHost, brokerPort, NetProtocolType.PROTOCOL_BUFFER);
			this.nMessages = nMessages;
			this.destinationType = destinationType;
			this.topic = topic;
			this.consumerReady  = consumerReady;
			this.consumerFinished = consumerFinished;
			bClient.connect();
		}

		@Override
		public void run() {
			try{
				bClient.subscribe(topic, destinationType, new BrokerListener() {
					
					@Override
					public void deliverMessage(NetMessage message, HostInfo host)
							throws Throwable {
						log.debug("Incoming message!");
						int totalMessagesConsumed = consumedMessages.incrementAndGet();
						if(totalMessagesConsumed == nMessages){
							consumerFinished.countDown();
						}
						bClient.acknowledge(message.getAction().getNotificationMessage(), host);
					}
				});
				consumerReady.countDown();
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		
	}
	
	@Test
	public void topicProducerAndConsumerMultipleAgents() throws InterruptedException{
		int totalMessages = 1000; //~5 minutes
		CountDownLatch consumerReady = new CountDownLatch(1);
		CountDownLatch producerFinished = new CountDownLatch(1);
		CountDownLatch consumerFinished = new CountDownLatch(1);
		Producer producer = new Producer(BROKER_AGENT_1_HOST, BROKER_AGENT_1_PORT, totalMessages, DestinationType.TOPIC, BROKER_TOPIC, producerFinished);
		Consumer consumer = new Consumer(BROKER_AGENT_2_HOST, BROKER_AGENT_2_PORT, totalMessages, DestinationType.TOPIC, BROKER_TOPIC, consumerReady, consumerFinished);
		new Thread(consumer).start();
		consumerReady.await();
		//Consumer is ready for shure at this point
		new Thread(producer).start();
		//Whait until all mesages are produced
		producerFinished.await();
		//At this point we whait until the consumer finishes or until a timeout expires
		consumerFinished.await(CONSUMER_WAIT_TIME_MILIS, TimeUnit.MILLISECONDS);
		assertEquals(producedMessages.get(), consumedMessages.get());
	}
	
	@Test
	public void virtualQueueProducerAndConsumerMultipleAgents() throws InterruptedException {
		int totalMessages = 1000; //~5 minutes
		CountDownLatch consumerReady = new CountDownLatch(1);
		CountDownLatch producerFinished = new CountDownLatch(1);
		CountDownLatch consumerFinished = new CountDownLatch(1);
		Producer producer = new Producer(BROKER_AGENT_1_HOST, BROKER_AGENT_1_PORT, totalMessages, DestinationType.TOPIC, BROKER_TOPIC, producerFinished);
		Consumer consumer = new Consumer(BROKER_AGENT_2_HOST, BROKER_AGENT_2_PORT, totalMessages, DestinationType.TOPIC, VIRTUALQUEUE_BROKER_TOPIC, consumerReady, consumerFinished);
		new Thread(consumer).start();
		consumerReady.await();
		//Consumer is ready for shure at this point
		new Thread(producer).start();
		//Whait until all mesages are produced
		producerFinished.await();
		//At this point we whait until the consumer finishes or until a timeout expires
		consumerFinished.await(CONSUMER_WAIT_TIME_MILIS, TimeUnit.MILLISECONDS);
		assertEquals(producedMessages.get(), consumedMessages.get());
	}
	
	@Test
	public void queueProducerAndConsumerMultipleAgents() throws InterruptedException{
		int totalMessages = 1000; //~5 minutes
		CountDownLatch consumerReady = new CountDownLatch(1);
		CountDownLatch producerFinished = new CountDownLatch(1);
		CountDownLatch consumerFinished = new CountDownLatch(1);
		Producer producer = new Producer(BROKER_AGENT_1_HOST, BROKER_AGENT_1_PORT, totalMessages, DestinationType.QUEUE, BROKER_QUEUE, producerFinished);
		Consumer consumer = new Consumer(BROKER_AGENT_2_HOST, BROKER_AGENT_2_PORT, totalMessages, DestinationType.QUEUE, BROKER_QUEUE, consumerReady, consumerFinished);
		new Thread(consumer).start();
		consumerReady.await();
		//Consumer is ready for shure at this point
		new Thread(producer).start();
		//Whait until all mesages are produced
		producerFinished.await();
		//At this point we whait until the consumer finishes or until a timeout expires
		consumerFinished.await(CONSUMER_WAIT_TIME_MILIS, TimeUnit.MILLISECONDS);
		assertEquals(producedMessages.get(), consumedMessages.get());
//		assertEquals(totalMessages, consumedMessages.get());
	}
	
}
