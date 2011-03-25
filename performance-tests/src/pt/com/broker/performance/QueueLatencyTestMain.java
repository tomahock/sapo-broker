package pt.com.broker.performance;

import java.util.concurrent.atomic.AtomicInteger;

import pt.com.broker.client.BrokerClient;
import pt.com.broker.client.messaging.BrokerListener;
import pt.com.broker.types.NetBrokerMessage;
import pt.com.broker.types.NetNotification;
import pt.com.broker.types.NetSubscribe;
import pt.com.broker.types.NetAction.DestinationType;

public class QueueLatencyTestMain
{
	/**
	 * Tests queue latency by measuring the time spent from between Enqueue and Received.
	 */

	// Test parameters
	private static final int NUMBER_OF_MESSAGES = 1000;
	private static final String BROKER_HOST = "127.0.0.1";
	private static final int BROKER_PORT = 3323;

	private static final String QUEUE_NAME = "/Queue/LatencyTest";

	// Broker
	private static final NetBrokerMessage message = new NetBrokerMessage("payload");
	private static BrokerClient brokerClient = null;

	// Test synchronization
	private static Object syncObj = new Object();
	
	// Test data
	private static AtomicInteger testCount = new AtomicInteger(0);
	private static long minLatency = Long.MAX_VALUE;
	private static long maxLatency = 0;
	private static long totalLantecy = 0;
	
	// Test Main
	public static void main(String[] args) throws Throwable
	{
		brokerClient = new BrokerClient(BROKER_HOST, BROKER_PORT);

		brokerClient.addAsyncConsumer(new NetSubscribe(QUEUE_NAME, DestinationType.QUEUE), new BrokerListener()
		{

			@Override
			public void onMessage(NetNotification message)
			{
				synchronized (syncObj)
				{
					syncObj.notifyAll();
				}
			}

			@Override
			public boolean isAutoAck()
			{
				return true;
			}
		});
		
		System.out.println();
		System.out.println("[Starting test]");
		
		runTest();
		brokerClient.close();
		
		// process and show test data
		System.out.println("Test results:");
		System.out.println(String.format("Min latency:		%s ms", minLatency / (1000* 1000)));
		System.out.println(String.format("Max latency:		%s ms", maxLatency / (1000* 1000)));
		System.out.println(String.format("Average latency:	%s ms", (totalLantecy/NUMBER_OF_MESSAGES) / (1000* 1000)));
	}

	private static void runTest() throws InterruptedException
	{
		do
		{
			if( (testCount.get()% 100) == 0)
			{
				System.err.println(testCount.get());
			}
			
			// send message
			brokerClient.enqueueMessage(message, QUEUE_NAME);
			
			// wait & mesure
			long start = System.nanoTime();	
			synchronized (syncObj)
			{
				syncObj.wait();
			}
			long end = System.nanoTime(); 
			
			// handle test data
			long time = end - start;
			if(time < minLatency)
			{
				minLatency = time;
			}
			if(time > maxLatency)
			{
				maxLatency = time;
			}
			totalLantecy += time;
		}
		while (testCount.incrementAndGet() != NUMBER_OF_MESSAGES);
	}

}
