package pt.com.broker.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.com.broker.types.NetBrokerMessage;

public class BrokerClientProducer {
	
	private static final Logger log = LoggerFactory.getLogger(BrokerClientProducer.class);
	
	private BrokerClient bk;
	
	public BrokerClientProducer(String host, int port){
		try {
			bk = new BrokerClient(host, port);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void produceMessage(String payload, String destination){
		NetBrokerMessage message = new NetBrokerMessage(payload);
		bk.publishMessage(message, destination);
	}

}
