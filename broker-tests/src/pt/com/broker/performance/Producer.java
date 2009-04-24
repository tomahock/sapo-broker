package pt.com.broker.performance;

import java.util.concurrent.Callable;

import pt.com.broker.client.BrokerClient;
import pt.com.types.NetBrokerMessage;
import pt.com.types.NetPublish;
import pt.com.types.NetAction.DestinationType;

public class Producer implements Callable<Integer>
{
	
	private final BrokerClient brokerClient;
	private final DestinationType destinationType;
	private final int numberOfMsgToSend;
	private final String message;

	public Producer(BrokerClient bkCLient, DestinationType destinationType, int numberOfMsgToSend, String message)
	{
		this.brokerClient = bkCLient;
		this.destinationType = destinationType;
		this.numberOfMsgToSend = numberOfMsgToSend;
		this.message = message;		
	}

	@Override
	public Integer call() throws Exception
	{
		NetBrokerMessage message = new NetBrokerMessage(this.message.getBytes());
		String destination = "/test/foo";
		if(destinationType == DestinationType.QUEUE)
		{
			for(int i = numberOfMsgToSend; i != 0; --i){
				brokerClient.enqueueMessage(message, destination, false, null);
			}
		}
		else
		{
			for(int i = numberOfMsgToSend; i != 0; --i){
				brokerClient.publishMessage(message, destination, false, null);
			}
		}
		
		
		return new Integer(0);
	}

}
